package de.hysky.skyblocker.utils.hoveredItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HoveredItemStackUtils {

	public static @Nullable HoveredItemStackProvider getProvider(Screen screen) {
		if (screen instanceof HoveredItemStackProvider) {
			return (HoveredItemStackProvider) screen;
		} else if (screen.getFocused() instanceof HoveredItemStackProvider) {
			return (HoveredItemStackProvider) screen.getFocused();
		}
		return null;
	}

	public static @Nullable ItemStack getHoveredItemStack(Screen screen) {
		HoveredItemStackProvider provider = getProvider(screen);
		if (provider == null) return null;

		return provider.getFocusedItem();
	}
}
