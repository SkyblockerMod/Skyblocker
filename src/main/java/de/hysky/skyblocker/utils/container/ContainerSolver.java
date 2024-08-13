package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ContainerSolver extends ContainerMatcher, Resettable {
	List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots);

	default void start(GenericContainerScreen screen) {}

	@Override
	default void reset() {}

	/**
	 * Called when the slot is clicked.
	 * @return {@code true} if the click should be canceled, {@code false} otherwise. Defaults to {@code false} if not overridden.
	 */
	default boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		return false;
	}

	static void trimEdges(Int2ObjectMap<ItemStack> slots, int rows) {
		for (int i = 0; i < rows; i++) {
			slots.remove(9 * i);
			slots.remove(9 * i + 8);
		}
		for (int i = 1; i < 8; i++) {
			slots.remove(i);
			slots.remove((rows - 1) * 9 + i);
		}
	}
}
