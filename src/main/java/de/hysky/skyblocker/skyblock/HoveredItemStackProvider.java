package de.hysky.skyblocker.skyblock;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface HoveredItemStackProvider {
	@Nullable ItemStack getFocusedItem();
}
