package de.hysky.skyblocker.utils.hoveredItem;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface HoveredItemStackProvider {
	@Nullable ItemStack getFocusedItem();
}
