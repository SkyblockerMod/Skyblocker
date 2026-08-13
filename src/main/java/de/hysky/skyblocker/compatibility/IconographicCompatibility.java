package de.hysky.skyblocker.compatibility;

import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public class IconographicCompatibility {
	public static boolean isEnabled = false;
	private static BiConsumer<ItemStack, Runnable> itemCallback = (_, runnable) -> runnable.run();

	public static void withItem(ItemStack item, Runnable runnable) {
		itemCallback.accept(item, runnable);
	}

	public static void setupItemCompat(BiConsumer<ItemStack, Runnable> callback) {
		isEnabled = true;
		itemCallback = callback;
	}
}
