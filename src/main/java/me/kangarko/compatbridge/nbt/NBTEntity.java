package me.kangarko.compatbridge.nbt;

import org.bukkit.entity.Entity;

/**
 * Represents an entity NBT tag
 */
public class NBTEntity extends NBTCompound {

	/**
	 * The entity associated with this tag
	 */
	private final Entity entity;

	/**
	 * Access an entity's NBT tag
	 */
	public NBTEntity(Entity entity) {
		super(null, null);

		this.entity = entity;
	}

	@Override
	protected Object getCompound() {
		return NBTReflectionUtil.getEntityNBTTagCompound(NBTReflectionUtil.getNMSEntity(entity));
	}

	@Override
	protected void setCompound(Object tag) {
		NBTReflectionUtil.setEntityNBTTag(tag, NBTReflectionUtil.getNMSEntity(entity));
	}

}
