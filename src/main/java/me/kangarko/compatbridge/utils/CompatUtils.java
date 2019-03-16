package me.kangarko.compatbridge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import me.kangarko.compatbridge.CompatBridge;
import me.kangarko.compatbridge.nbt.NBTItem;
import me.kangarko.compatbridge.utils.MinecraftVersion.V;

/**
 * Various utility class.
 *
 * @author kangarko
 */
public final class CompatUtils {

	// The "command sender", is the object that is responsible for sending messages to the console, colors are supported
	private static final ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

	// Is Minecraft older than 1.13? Storing here for best performance.
	private static final boolean LEGACY_MATERIALS = MinecraftVersion.olderThan(V.v1_13);

	// Prevent new instance, always call static methods
	private CompatUtils() {
	}

	/**
	 * Send a message to the player, with & letters colorized.
	 *
	 * @param sender the sender
	 * @param message the message
	 */
	public static final void tell(CommandSender sender, String message) {
		sender.sendMessage(colorize(message));
	}

	/**
	 * Replace the & letter with the {@link org.bukkit.ChatColor.COLOR_CHAR} in the message.
	 *
	 * @param message the message to replace color codes with '&'
	 * @return the colored message
	 */
	public static final String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/**
	 * Logs an error the the console only if debug is enabled
	 *
	 * @param message the message
	 * @param t the error
	 */
	public static final void debug(String message, Throwable t) {
		if (CompatBridge.isDebugEnabled())
			error(message, t);
	}

	/**
	 * Logs the error with a message to the console
	 *
	 * @param message the message
	 * @param t the error
	 */
	public static final void error(String message, Throwable t) {
		Bukkit.getLogger().log(Level.SEVERE, message, t);
	}

	/**
	 * Send a debug message to the console in case debug is enabled, see {@link CompatBridge#isDebugEnabled()}
	 *
	 * @param message the message to send
	 */
	public static final void debug(String message) {
		if (CompatBridge.isDebugEnabled())
			log(message);
	}

	/**
	 * Sends a colored message to the console.
	 *
	 * @param message the message to send
	 */
	public static final void log(String message) {
		consoleSender.sendMessage(colorize(message));
	}

	/**
	 * Replaces _ with space and capitalizes each word.
	 *
	 * @param myEnum the enum to bountify
	 * @return the pretty string
	 */
	public static final String bountify(Enum<?> myEnum) {
		return WordUtils.capitalizeFully(myEnum.name().toLowerCase().replace("_", " "));
	}

	/**
	 * See {@link #range(int, int, int)}
	 *
	 * @param value the real value
	 * @param min the min limit
	 * @param max the max limit
	 * @return the value in range
	 */
	public static final double range(double value, double min, double max) {
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Get a value in range. If the value is < min, returns min, if it is > max, returns max.
	 *
	 * @param value the real value
	 * @param min the min limit
	 * @param max the max limit
	 * @return the value in range
	 */
	public static final int range(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	/**
	 * Return an empty String if the String is null.
	 *
	 * @param string the string to check
	 * @return string or empty "" if the given arg is null
	 */
	public static final String getOrEmpty(String string) {
		return string != null ? string : "";
	}

	/**
	 * Returns the value or its default counterpart in case it is null
	 *
	 * @param value the primary value
	 * @param def the default value
	 * @return the value, or default it the value is null
	 */
	public static final <T> T getOrDefault(T value, T def) {
		Objects.requireNonNull(def, "The default value must not be null!");

		return value != null ? value : def;
	}

	/**
	 * Converts {@link Iterable} to {@link List}
	 *
	 * @param it the iterable
	 * @return the new list
	 */
	public static final <T> List<T> toList(Iterable<T> it) {
		final List<T> list = new ArrayList<>();
		it.forEach((el) -> list.add(el));

		return list;
	}

	/**
	 * Converts a list having one type object into another
	 *
	 * @param oldList the old list
	 * @param converter the converter
	 * @return the new list
	 */
	public static final <A, B> List<B> convert(List<A> oldList, TypeConverter<A, B> converter) {
		final List<B> newList = new ArrayList<>();

		for (final A oldKey : oldList)
			newList.add(converter.convert(oldKey));

		return newList;
	}

	/**
	 * Compare two lists. Two lists are considered equal if they are same length and all values are the same.
	 * Exception: Strings are stripped of colors before comparation.
	 *
	 * @param first, first list to compare
	 * @param second, second list to compare with
	 * @return true if lists are equal
	 */
	public static final <T> boolean listEquals(List<T> first, List<T> second) {
		if (first == null && second == null)
			return true;

		if (first == null && second != null)
			return false;

		if (first != null && second == null)
			return false;

		if (first != null) {
			if (first.size() != second.size())
				return false;

			for (int i = 0; i < first.size(); i++) {
				final T f = first.get(i);
				final T s = second.get(i);

				if (f == null && s != null)
					return false;

				if (f != null && s == null)
					return false;

				if (f != null && s != null && !f.equals(s))
					if (!ChatColor.stripColor(f.toString()).equals(ChatColor.stripColor(s.toString())))
						return false;
			}
		}

		return true;
	}

	/**
	 * Compares two items. Returns true if they are similar.
	 *
	 * Two items are similar if both are not null and if their type, data, name and lore equals.
	 * The damage, quantity, item flags enchants and other properties are ignored.
	 *
	 * @param first
	 * @param second
	 * @return true if items are similar (see above)
	 */
	public static final boolean isSimilar(ItemStack first, ItemStack second) {
		if (first == null || second == null)
			return false;

		final boolean idMatch = first.getType() == second.getType();
		boolean dataMatch = LEGACY_MATERIALS ? first.getData().getData() == second.getData().getData() : true;
		final boolean metaMatch = first.hasItemMeta() == second.hasItemMeta();

		if (!idMatch || !metaMatch || !(dataMatch || (dataMatch = first.getType() == Material.BOW))) {
			return false;
		}

		// ItemMeta
		{
			final ItemMeta f = first.getItemMeta();
			final ItemMeta s = second.getItemMeta();

			final String fName = ChatColor.stripColor( getOrEmpty(f.getDisplayName()).toLowerCase() );
			final String sName = ChatColor.stripColor( getOrEmpty(s.getDisplayName()).toLowerCase() );

			if (!fName.equals(sName) || !listEquals(f.getLore(), s.getLore()))
				return false;
		}

		final NBTItem firstNbt = new NBTItem(first);
		final NBTItem secondNbt = new NBTItem(second);

		return matchNbt(CompatBridge.getPlugin().getName(), firstNbt, secondNbt) && matchNbt(CompatBridge.getPlugin().getName() + "_Item", firstNbt, secondNbt);
	}

	// Compares the NBT string tag of two items
	private static final boolean matchNbt(String key, NBTItem firstNbt, NBTItem secondNbt) {
		final boolean firstHas = firstNbt.hasKey(key);
		final boolean secondHas = secondNbt.hasKey(key);

		if (!firstHas && !secondHas)
			return true; // nothing has, essentially same

		else if (firstHas && !secondHas || !firstHas && secondHas)
			return false; // one has but another hasn't, cannot be same

		return firstNbt.getString(key).equals(secondNbt.getString(key));
	}

	/**
	 * Runs the task if the plugin is enabled correctly
	 *
	 * @param task the task
	 * @return the task or null
	 */
	public static final BukkitTask runDelayed(Runnable task) {
		return runDelayed(1, task);
	}

	/**
	 * Runs the task even if the plugin is disabled for some reason.
	 *
	 * @param delayTicks
	 * @param task
	 * @return the task or null
	 */
	public static final BukkitTask runDelayed(int delayTicks, Runnable task) {
		final BukkitScheduler scheduler = Bukkit.getScheduler();
		final JavaPlugin instance = CompatBridge.getPlugin();

		return runIfDisabled(task) ? null : delayTicks == 0 ? scheduler.runTask(instance, task) : scheduler.runTaskLater(instance, task, delayTicks);
	}

	/**
	 * Runs the task async even if the plugin is disabled for some reason.
	 *
	 * @param delayTicks
	 * @param task
	 * @return the task or null
	 */
	public static final BukkitTask runDelayedAsync(int delayTicks, Runnable task) {
		final BukkitScheduler scheduler = Bukkit.getScheduler();
		final JavaPlugin instance = CompatBridge.getPlugin();

		return runIfDisabled(task) ? null : delayTicks == 0 ? scheduler.runTaskAsynchronously(instance, task) : scheduler.runTaskLaterAsynchronously(instance, task, delayTicks);
	}

	/**
	 * Runs the task timer even if the plugin is disabled.
	 *
	 * @param repeatTicks the delay between each execution
	 * @param task the task
	 * @return the bukkit task or null
	 */
	public static BukkitTask runTimer(int repeatTicks, Runnable task) {
		return runTimer(0, repeatTicks, task);
	}


	/**
	 *	Runs the task timer even if the plugin is disabled.
	 *
	 * @param delayTicks the delay before first run
	 * @param repeatTicks the delay between each run
	 * @param task the task
	 * @return the bukkit task or null if error
	 */
	public static BukkitTask runTimer(int delayTicks, int repeatTicks, Runnable task) {
		return runIfDisabled(task) ? null : Bukkit.getScheduler().runTaskTimer(CompatBridge.getPlugin(), task, delayTicks, repeatTicks);
	}

	// Check our plugin instance if it's enabled
	// In case it is disabled, just runs the task and returns true
	// Otherwise we return false and the task will be run correctly in Bukkit scheduler
	// This is fail-safe to critical save-on-exit operations in case our plugin is improperly reloaded (PlugMan) or malfunctions
	private static final boolean runIfDisabled(Runnable run) {
		if (!CompatBridge.getPlugin().isEnabled()) {
			run.run();

			return true;
		}

		return false;
	}

	/**
	 * Call an event in Bukkit and return whether it was NOT cancelled
	 *
	 * @param event the event
	 * @return true if the event was not cancelled
	 */
	public static final boolean callEvent(Event event) {
		Bukkit.getPluginManager().callEvent(event);

		return event instanceof Cancellable ? !((Cancellable)event).isCancelled() : true;
	}

	/**
	 * A simple interface to convert between types
	 *
	 * @param <A> the initial type to convert from
	 * @param <B> the final type to convert to
	 */
	public interface TypeConverter<A, B> {

		/**
		 * Convert a type given from A to B
		 *
		 * @param value the old value type
		 * @return the new value type
		 */
		public B convert(A value);
	}
}
