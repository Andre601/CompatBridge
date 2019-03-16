package me.kangarko.compatbridge.nbt;

import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents an item's NBT tag
 */
@Setter
@Getter
public class NBTItem extends NBTCompound {

	/**
	 * The associated item stack
	 */
	private ItemStack item;

	/**
	 * Access an items's NBT tag
	 */
	public NBTItem(@NonNull ItemStack item) {
		super(null, null);
		this.item = item.clone();
	}

	@Override
	protected Object getCompound() {
		return NBTReflectionUtil.getItemRootNBTTagCompound(NBTReflectionUtil.getNMSItemStack(item));
	}

	@Override
	protected void setCompound(Object tag) {
		item = NBTReflectionUtil.getBukkitItemStack(NBTReflectionUtil.setNBTTag(tag, NBTReflectionUtil.getNMSItemStack(item)));
	}
}
