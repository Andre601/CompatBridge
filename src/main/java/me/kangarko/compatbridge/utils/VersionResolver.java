package me.kangarko.compatbridge.utils;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import lombok.Getter;

/**
 * Utility class helps to determine which MC version is installed.
 */
public class VersionResolver {

	/**
	 * Detects if we run at least Minecraft 1.9.
	 */
	@Getter
	private static boolean atLeast1_9 = true;

	/**
	 * Detects if we run at least Minecraft 1.8.
	 */
	@Getter
	private static boolean atLeast1_8 = true;

	/**
	 * Detects if we run at least Minecraft 1.12.
	 */
	@Getter
	private static boolean atLeast1_12 = true;

	/**
	 * Detects if we run at least Minecraft 1.13 or newer with new material names.
	 */
	@Getter
	private static boolean atLeast1_13 = true;

	static {
		// MC 1.8
		try {
			Material.valueOf("PRISMARINE");

		} catch (final IllegalArgumentException ex) {
			atLeast1_8 = false;
		}

		// MC 1.9
		try {
			Sound.valueOf("BLOCK_END_GATEWAY_SPAWN").ordinal();

		} catch (final Throwable t) {
			atLeast1_9 = false;
		}

		// MC 1.12
		try {
			EntityType.valueOf("ILLUSIONER");

		} catch (final Throwable t) {
			atLeast1_12 = false;
		}

		// MC 1.13
		try {
			Material.valueOf("TRIDENT");

		} catch (final IllegalArgumentException ex) {
			atLeast1_13 = false;
		}
	}
}
