package me.kangarko.compatbridge.model;

import org.bukkit.DyeColor;

public enum CompDye {

	WHITE,
	ORANGE,
	MAGENTA,
	LIGHT_BLUE,
	YELLOW,
	LIME,
	PINK,
	GRAY,
	LIGHT_GRAY("SILVER"),
	CYAN,
	PURPLE,
	BLUE,
	BROWN,
	GREEN,
	RED,
	BLACK;

	private final DyeColor dye;

	private CompDye() {
		this(null);
	}

	private CompDye(String name) {
		DyeColor color;

		try {
			color = DyeColor.valueOf(name());

		} catch (final IllegalArgumentException ex) {
			if (name == null) throw new RuntimeException("Missing legacy name for DyeColor." + name());

			color = DyeColor.valueOf(name);
		}

		if (color == null) throw new RuntimeException("Failed to resolve DyeColor." + name());
		this.dye = color;
	}

	public DyeColor getDye() {
		return dye;
	}

	public static final CompDye fromWoolData(byte data) {
		return fromDye(DyeColor.getByWoolData(data));
	}

	public static final CompDye fromDye(DyeColor dye) {
		for (final CompDye comp : values())
			if (comp.getDye() == dye)
				return comp;

		throw new RuntimeException("Could not get CompDye from DyeColor." + dye.toString());
	}
}