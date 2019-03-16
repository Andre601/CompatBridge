package me.kangarko.compatbridge.model;

import java.util.Objects;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * Wrapper for {@link Attribute}
 */
public enum CompAttribute {

	/**
	 * Maximum health of an Entity.
	 */
	GENERIC_MAX_HEALTH,
	/**
	 * Range at which an Entity will follow others.
	 */
	GENERIC_FOLLOW_RANGE,
	/**
	 * Resistance of an Entity to knockback.
	 */
	GENERIC_KNOCKBACK_RESISTANCE,
	/**
	 * Movement speed of an Entity.
	 */
	GENERIC_MOVEMENT_SPEED,
	/**
	 * Flying speed of an Entity.
	 */
	GENERIC_FLYING_SPEED,
	/**
	 * Attack damage of an Entity.
	 */
	GENERIC_ATTACK_DAMAGE,
	/**
	 * Attack speed of an Entity.
	 */
	GENERIC_ATTACK_SPEED,
	/**
	 * Armor bonus of an Entity.
	 */
	GENERIC_ARMOR,
	/**
	 * Armor durability bonus of an Entity.
	 */
	GENERIC_ARMOR_TOUGHNESS,
	/**
	 * Luck bonus of an Entity.
	 */
	GENERIC_LUCK,
	/**
	 * Strength with which a horse will jump.
	 */
	HORSE_JUMP_STRENGTH,
	/**
	 * Chance of a zombie to spawn reinforcements.
	 */
	ZOMBIE_SPAWN_REINFORCEMENTS;

	/**
	 * Finds the attribute of an entity
	 *
	 * @param entity
	 * @return the attribute, or null if not supported by the server
	 */
	public final Double get(LivingEntity entity) {
		try {
			final AttributeInstance instance = entity.getAttribute(Attribute.valueOf(toString()));

			return instance != null ? instance.getBaseValue() : null;

		} catch (IllegalArgumentException | NoSuchMethodError | NoClassDefFoundError ex) {
			return null;
		}
	}

	/**
	 * If supported by the server, sets a new attribute to the entity
	 *
	 * @param entity
	 * @param value
	 */
	public final void set(LivingEntity entity, double value) {
		try {
			Objects.requireNonNull(entity, "Entity cannot be null");
			Objects.requireNonNull(entity, "Attribute cannot be null");

			final AttributeInstance instance = entity.getAttribute(Attribute.valueOf(toString()));

			instance.setBaseValue(value);
		} catch (NoSuchMethodError | NoClassDefFoundError ex) {}
	}
}