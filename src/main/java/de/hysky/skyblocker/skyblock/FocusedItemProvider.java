package de.hysky.skyblocker.skyblock;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FocusedItemProvider {
	private static @Nullable ItemStack focusedItem = null;

	public static void setFocusedItem(@Nullable ItemStack stack) {
		focusedItem = stack;
	}

	public static @Nullable ItemStack getFocusedItem() {
		return focusedItem;
	}
}
