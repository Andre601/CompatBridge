package me.kangarko.compatbridge.bar;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;

import me.kangarko.compatbridge.CompatBridge;
import me.kangarko.compatbridge.model.CompBarColor;
import me.kangarko.compatbridge.model.CompBarStyle;
import me.kangarko.compatbridge.utils.CompatUtils;
import me.kangarko.compatbridge.utils.MinecraftVersion;
import me.kangarko.compatbridge.utils.MinecraftVersion.V;
import me.kangarko.compatbridge.utils.ReflectionUtil;

/**
 * The classes handling Boss Bar cross-server compatibility are based off of the code by SoThatsIt.
 *
 * http://forums.bukkit.org/threads/tutorial-utilizing-the-boss-health-bar.158018/page-2#post-1760928
 *
 * @deprecated please use our main compat class to call this
 */
@Deprecated
public class BarBridge implements Listener {

	/**
	 * Synchronize on the main thread
	 */
	private static final Object LOCK = new Object();

	/**
	 * The fake dragon class
	 */
	private static Class<?> entityClass;

	/**
	 * Does the current MC version require us to spawn the dragon below ground?
	 */
	private static boolean isBelowGround = true;

	/**
	 * The player currently viewing the boss bar
	 */
	private static HashMap<UUID, BarDragonEntity> players = new HashMap<>();

	/**
	 * Currently running timers (for temporary boss bars)
	 */
	private static HashMap<UUID, Integer> timers = new HashMap<>();

	/**
	 * The singleton instance
	 */
	private static BarBridge singleton = null;

	// Singleton
	private BarBridge() {
	}

	// Initialize reflection and start listening to events
	static {
		if (ReflectionUtil.isProtocolHack()) {
			entityClass = v1_8Fake.class;
			isBelowGround = false;

		} else {
			if (MinecraftVersion.equals(V.v1_6))
				entityClass = v1_6.class;

			else if (MinecraftVersion.equals(V.v1_7))
				entityClass = v1_7.class;

			else if (MinecraftVersion.equals(V.v1_8))  {
				entityClass = v1_8.class;
				isBelowGround = false;

			} else if (MinecraftVersion.newerThan(V.v1_8))
				entityClass = v1_9_bukkit.class;
		}

		Objects.requireNonNull(entityClass, "CompatBridge does not support Boss bar on MC version " + MinecraftVersion.getServerVersion() + "!");

		if (singleton == null && CompatBridge.getPlugin().isEnabled()) {
			singleton = new BarBridge();

			Bukkit.getPluginManager().registerEvents(singleton, CompatBridge.getPlugin());

			if (ReflectionUtil.isProtocolHack())
				CompatUtils.runTimer(5, () -> {
					for (final UUID uuid : players.keySet()) {
						final Player player = CompatBridge.getPlayerByUUID(uuid);

						ReflectionUtil.sendPacket(player, players.get(uuid).getTeleportPacket(getDragonLocation(player.getLocation())));
					}
				});
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPluginDisable(PluginDisableEvent e) {
		if (e.getPlugin().equals(CompatBridge.getPlugin()) && singleton != null)
			singleton.stop();
	}

	// Removes bars from all players
	private void stop() {
		for (final Player player : CompatBridge.getOnlinePlayers())
			removeBar(player);

		players.clear();

		for (final int timerID : timers.values())
			Bukkit.getScheduler().cancelTask(timerID);
		timers.clear();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		removeBar(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		removeBar(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		handleTeleport(event.getPlayer(), event.getTo().clone());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerRespawnEvent event) {
		handleTeleport(event.getPlayer(), event.getRespawnLocation().clone());
	}

	// Fixes bar disappearing on teleport
	private void handleTeleport(final Player player, final Location loc) {
		if (!hasBar(player))
			return;

		final BarDragonEntity oldDragon = getDragon(player, "");

		if (oldDragon instanceof v1_9_bukkit)
			return;

		CompatUtils.runDelayed(2, () -> {
			if (!hasBar(player))
				return;

			final float health = oldDragon.health;
			final String message = oldDragon.name;

			ReflectionUtil.sendPacket(player, getDragon(player, "").getDestroyPacket());

			players.remove(player.getUniqueId());

			final BarDragonEntity dragon = addDragon(player, loc, message);
			dragon.health = health;

			sendDragon(dragon, player);
		});
	}

	/**
	 * Set a message for the given player.<br>
	 * It will remain there until the player logs off or another plugin overrides it.<br>
	 * This method will show a health bar using the given percentage value and will cancel any running timers.
	 *
	 * @param player  The player who should see the given message.
	 * @param message The message shown to the player.<br>
	 *                Due to limitations in Minecraft this message cannot be longer than 64 characters.<br>
	 *                It will be cut to that size automatically.
	 * @param percent The percentage of the health bar filled.<br>
	 *                This value must be between 0F (inclusive) and 100F (inclusive).
	 *
	 * @throws IllegalArgumentException If the percentage is not within valid bounds.
	 */
	public static void setMessage(Player player, String message, float percent, CompBarColor color, CompBarStyle style) {
		Validate.isTrue(0F <= percent && percent <= 100F, "Percent must be between 0F and 100F, but was: " + percent);

		if (hasBar(player))
			removeBar(player);

		final BarDragonEntity dragon = getDragon(player, message);

		dragon.name = cleanMessage(message);
		dragon.health = (percent / 100f) * dragon.getMaxHealth();

		if (color != null)
			dragon.barColor = color;
		if (style != null)
			dragon.barStyle = style;

		cancelTimer(player);

		sendDragon(dragon, player);
	}

	/**
	 * Set a message for the given player.<br>
	 * It will remain there until the player logs off or another plugin overrides it.<br>
	 * This method will use the health bar as a decreasing timer, all previously started timers will be cancelled.<br>
	 * The timer starts with a full bar.<br>
	 * The health bar will be removed automatically if it hits zero.
	 *
	 * @param player  The player who should see the given timer/message.
	 * @param message The message shown to the player.<br>
	 *                Due to limitations in Minecraft this message cannot be longer than 64 characters.<br>
	 *                It will be cut to that size automatically.
	 * @param seconds The amount of seconds displayed by the timer.<br>
	 *                Supports values above 1 (inclusive).
	 *
	 * @throws IllegalArgumentException If seconds is zero or below.
	 */
	public static void setMessage(final Player player, String message, int seconds, CompBarColor color, CompBarStyle style) {
		Validate.isTrue(seconds > 0, "Seconds must be > 1 ");

		if (hasBar(player))
			removeBar(player);

		final BarDragonEntity dragon = getDragon(player, message);

		dragon.name = cleanMessage(message);
		dragon.health = dragon.getMaxHealth();

		if (color != null)
			dragon.barColor = color;
		if (style != null)
			dragon.barStyle = style;

		final float dragonHealthMinus = dragon.getMaxHealth() / seconds;

		cancelTimer(player);

		timers.put(player.getUniqueId(), CompatUtils.runTimer(20, 20, () -> {
			final BarDragonEntity drag = getDragon(player, "");
			drag.health -= dragonHealthMinus;

			if (drag.health <= 1) {
				removeBar(player);
				cancelTimer(player);
			} else {
				sendDragon(drag, player);
			}
		}).getTaskId());

		sendDragon(dragon, player);
	}

	private static void removeBar(Player player) {
		if (!hasBar(player))
			return;

		final BarDragonEntity dragon = getDragon(player, "");

		if (dragon instanceof v1_9_bukkit) {
			((v1_9_bukkit) dragon).removePlayer(player);
		} else
			ReflectionUtil.sendPacket(player, getDragon(player, "").getDestroyPacket());

		players.remove(player.getUniqueId());

		cancelTimer(player);
	}

	private static boolean hasBar(Player player) {
		return players.containsKey(player.getUniqueId());
	}

	private static String cleanMessage(String message) {
		if (message.length() > 64)
			message = message.substring(0, 63);

		return message;
	}

	private static void cancelTimer(Player player) {
		final Integer timerID = timers.remove(player.getUniqueId());

		if (timerID != null) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
	}

	private static void sendDragon(BarDragonEntity dragon, Player player) {
		if (dragon instanceof v1_9_bukkit) {
			final v1_9_bukkit bar = (v1_9_bukkit) dragon;

			bar.addPlayer(player);
			bar.setProgress(dragon.health / dragon.getMaxHealth());
		} else {
			ReflectionUtil.sendPacket(player, dragon.getMetaPacket(dragon.getWatcher()));
			ReflectionUtil.sendPacket(player, dragon.getTeleportPacket(getDragonLocation(player.getLocation())));
		}
	}

	private static BarDragonEntity getDragon(Player player, String message) {
		if (hasBar(player))
			return players.get(player.getUniqueId());

		return addDragon(player, cleanMessage(message));
	}

	private static BarDragonEntity addDragon(Player player, String message) {
		final BarDragonEntity dragon = newDragon(message, getDragonLocation(player.getLocation()));

		if (dragon instanceof v1_9_bukkit)
			((v1_9_bukkit) dragon).addPlayer(player);

		else
			ReflectionUtil.sendPacket(player, dragon.getSpawnPacket());

		players.put(player.getUniqueId(), dragon);

		return dragon;
	}

	private static BarDragonEntity addDragon(Player player, Location loc, String message) {
		final BarDragonEntity dragon = newDragon(message, getDragonLocation(loc));

		if (dragon instanceof v1_9_bukkit)
			((v1_9_bukkit) dragon).addPlayer(player);

		else
			ReflectionUtil.sendPacket(player, dragon.getSpawnPacket());

		players.put(player.getUniqueId(), dragon);

		return dragon;
	}

	private static Location getDragonLocation(Location loc) {
		if (isBelowGround) {
			loc.subtract(0, 300, 0);
			return loc;
		}

		final float pitch = loc.getPitch();

		if (pitch >= 55)
			loc.add(0, -300, 0);
		else if (pitch <= -55)
			loc.add(0, 300, 0);
		else
			loc = loc.getBlock().getRelative(getDirection(loc), (Bukkit.getViewDistance() * 16)).getLocation();

		return loc;
	}

	private static BlockFace getDirection(Location loc) {
		final float dir = Math.round(loc.getYaw() / 90);
		if (dir == -4 || dir == 0 || dir == 4)
			return BlockFace.SOUTH;
		if (dir == -1 || dir == 3)
			return BlockFace.EAST;
		if (dir == -2 || dir == 2)
			return BlockFace.NORTH;
		if (dir == -3 || dir == 1)
			return BlockFace.WEST;
		return null;
	}

	private static BarDragonEntity newDragon(String message, Location loc) {
		synchronized (LOCK) {
			BarDragonEntity fakeDragon = null;

			try {
				fakeDragon = (BarDragonEntity) entityClass.getConstructor(String.class, Location.class).newInstance(message, loc);
			} catch (final ReflectiveOperationException e) {
				e.printStackTrace();
			}

			return fakeDragon;
		}
	}
}