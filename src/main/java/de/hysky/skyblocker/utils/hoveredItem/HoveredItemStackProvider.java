package de.hysky.skyblocker.utils.hoveredItem;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

public interface HoveredItemStackProvider {
	@Nullable ItemStack getFocusedItem();
}
