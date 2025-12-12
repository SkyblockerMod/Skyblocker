package de.hysky.skyblocker.utils.hoveredItem;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public class HoveredItemStackUtils {

	public static @Nullable HoveredItemStackProvider getProvider(Screen screen) {
		if (screen instanceof HoveredItemStackProvider provider) {
			return provider;
		} else if (screen.getFocused() instanceof HoveredItemStackProvider provider) {
			return provider;
		}
		return null;
	}

	public static @Nullable ItemStack getHoveredItemStack(Screen screen) {
		HoveredItemStackProvider provider = getProvider(screen);
		if (provider == null) return null;

		return provider.getFocusedItem();
	}
}
