package de.hysky.skyblocker.utils.hoveredItem;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
