package me.kangarko.compatbridge.utils;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * Utility class helps to determine which MC version is installed.
 */
public class VersionResolver {

	/**
	 * Detects if we run Minecraft 1.9.
	 */
	public static boolean MC_1_9 = true;

	/**
	 * Detects if we run Minecraft 1.8.
	 */
	public static boolean MC_1_8 = true;

	/**
	 * Detects if we run Minecraft 1.13 or newer with new material names.
	 */
	public static boolean MC_1_13 = true;

	static {
		// MC 1.13
		try {
			Material.valueOf("TRIDENT");

		} catch (final IllegalArgumentException ex) {
			MC_1_13 = false;
		}

		// MC 1.9
		try {
			Sound.valueOf("BLOCK_END_GATEWAY_SPAWN").ordinal();

		} catch (final Throwable t) {
			MC_1_9 = false;
		}

		// MC 1.8
		try {
			Material.valueOf("PRISMARINE");

		} catch (final IllegalArgumentException ex) {
			MC_1_8 = false;
		}
	}

	public static final boolean isAtLeast1_13() {
		return MC_1_13;
	}

	public static final boolean isAtLeast1_9() {
		return MC_1_9;
	}

	public static final boolean isAtLeast1_8() {
		return MC_1_8;
	}
}
