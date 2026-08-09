package de.hysky.skyblocker.compatibility;

import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public class IconographicCompatibility {
	public static BiConsumer<ItemStack, Runnable> itemCallback = (_, runnable) -> runnable.run();

	public static void withItem(ItemStack item, Runnable runnable) {
		itemCallback.accept(item, runnable);
	}

	public static void setupItemCompat(BiConsumer<ItemStack, Runnable> callback) {
		itemCallback = callback;
	}
}
