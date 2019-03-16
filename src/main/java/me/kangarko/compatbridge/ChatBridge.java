package me.kangarko.compatbridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.kangarko.compatbridge.utils.CompatUtils;
import me.kangarko.compatbridge.utils.MinecraftVersion;
import me.kangarko.compatbridge.utils.MinecraftVersion.V;
import me.kangarko.compatbridge.utils.ReflectionUtil;
import me.kangarko.compatbridge.utils.ReflectionUtil.ReflectionException;

/**
 * Reflection class for handling chat-related methods
 *
 * See {@link CompatBridge}
 */
class ChatBridge {

	private static Object enumTitle;
	private static Object enumSubtitle;
	private static Object enumReset;

	private static Constructor<?> tabConstructor;

	private static Constructor<?> titleTimesConstructor;
	private static Constructor<?> titleConstructor;
	private static Constructor<?> subtitleConstructor;
	private static Constructor<?> resetTitleConstructor;

	private static final Method componentSerializer;
	private static final Constructor<?> chatMessageConstructor;

	// Prevent new instance, always call static methods
	public ChatBridge() {
	}

	static {
		try {
			final Class<?> chatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");

			Class<?> serializer = null;
			if (MinecraftVersion.newerThan(V.v1_7))
				serializer = chatBaseComponent.getDeclaredClasses()[0];
			else
				serializer = ReflectionUtil.getNMSClass("ChatSerializer");

			componentSerializer = serializer.getMethod("a", String.class);

			final Class<?> chatPacket = ReflectionUtil.getNMSClass("PacketPlayOutChat");
			chatMessageConstructor = MinecraftVersion.newerThan(V.v1_7) ? chatPacket.getConstructor(chatBaseComponent, byte.class) : chatPacket.getConstructor(chatBaseComponent);

			if (MinecraftVersion.newerThan(V.v1_7)) {
				final Class<?> titlePacket = ReflectionUtil.getNMSClass("PacketPlayOutTitle");
				final Class<?> enumAction = titlePacket.getDeclaredClasses()[0];

				enumTitle = enumAction.getField("TITLE").get(null);
				enumSubtitle = enumAction.getField("SUBTITLE").get(null);
				enumReset = enumAction.getField("RESET").get(null);

				tabConstructor = ReflectionUtil.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(chatBaseComponent);

				titleTimesConstructor = titlePacket.getConstructor(int.class, int.class, int.class);
				titleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
				subtitleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
				resetTitleConstructor = titlePacket.getConstructor(enumAction, chatBaseComponent);
			}

		} catch (final Exception t) {
			t.printStackTrace();

			throw new ReflectionException("Error initiating Chat/Title/ActionBAR API (incompatible Craftbukkit? - " + Bukkit.getVersion() + " / " + Bukkit.getBukkitVersion() + " / " + MinecraftVersion.getServerVersion() + ")", t);
		}
	}

	/**
	 * Send a title to player
	 *
	 * @param player
	 * @param fadeIn
	 * @param stay
	 * @param fadeOut
	 * @param title
	 * @param subtitle
	 */
	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
		try {
			if (titleConstructor == null)
				return;

			resetTitle(player);

			if (titleTimesConstructor != null) {
				final Object packet = titleTimesConstructor.newInstance(fadeIn, stay, fadeOut);

				ReflectionUtil.sendPacket(player, packet);
			}

			if (title != null) {
				final Object chatTitle = serializeText(title);
				final Object packet = titleConstructor.newInstance(enumTitle, chatTitle);

				ReflectionUtil.sendPacket(player, packet);
			}

			if (subtitle != null) {
				final Object chatSubtitle = serializeText(subtitle);
				final Object packet = subtitleConstructor.newInstance(enumSubtitle, chatSubtitle);

				ReflectionUtil.sendPacket(player, packet);
			}
		} catch (final ReflectiveOperationException ex) {
			throw new ReflectionException("Error sending title to: " + player.getName(), ex);
		}
	}

	/**
	 * Reset title for player
	 *
	 * @param player
	 */
	public static void resetTitle(Player player) {
		try {
			if (resetTitleConstructor == null)
				return;

			final Object packet = resetTitleConstructor.newInstance(enumReset, null);

			ReflectionUtil.sendPacket(player, packet);
		} catch (final ReflectiveOperationException ex) {
			throw new ReflectionException("Error resetting title to: " + player.getName());
		}
	}

	/**
	 * Send tablist to player
	 *
	 * @param player
	 * @param headerRaw
	 * @param footerRaw
	 */
	public static void sendTablist(Player player, String headerRaw, String footerRaw) {
		try {
			if (tabConstructor == null)
				return;

			final Object header = serializeText(headerRaw);
			final Object packet = tabConstructor.newInstance(header);

			if (footerRaw != null) {
				final Object footer = serializeText(footerRaw);

				final Field f = packet.getClass().getDeclaredField("b"); // setFooter
				f.setAccessible(true);
				f.set(packet, footer);
			}

			ReflectionUtil.sendPacket(player, packet);

		} catch (final ReflectiveOperationException ex) {
			CompatUtils.error("Failed to send tablist to " + player.getName() + ", title: " + headerRaw + " " + footerRaw, ex);
		}
	}

	/**
	 * Send action bar to player
	 *
	 * @param player
	 * @param message
	 */
	public static void sendActionBar(Player player, String message) {
		sendChat(player, message, (byte) 2);
	}

	private static void sendChat(Player pl, String text, byte type) {
		try {
			final Object message = serializeText(text);
			Objects.requireNonNull(message, "Message cannot be null!");

			final Object packet = chatMessageConstructor.newInstance(message, type); // http://wiki.vg/Protocol#Chat_Message
			ReflectionUtil.sendPacket(pl, packet);

		} catch (final ReflectiveOperationException ex) {
			CompatUtils.error("Failed to send chat packet type " + type + " to " + pl.getName() + ", message: " + text, ex);
		}
	}

	private static Object serializeText(String text) throws ReflectiveOperationException {
		text = removeBracketsAndColorize(text);

		try {
			return componentSerializer.invoke(null, "{\"text\":\"" + Matcher.quoteReplacement(text) +  "\"}");

		} catch (final Throwable t) {
			throw new RuntimeException("Failed to serialize text: " + text, t);
		}
	}

	private static String removeBracketsAndColorize(String text) {
		if (text == null)
			return "";

		if ((text.startsWith("\"") && text.endsWith("\"")) || text.startsWith("'") && text.endsWith("'"))
			text = text.substring(1, text.length() - 1);

		return CompatUtils.colorize(text);
	}
}
