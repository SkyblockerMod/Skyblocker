package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ContainerSolver extends ContainerMatcher {
	List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots);

	default void start(GenericContainerScreen screen) {}

	default void reset() {}

	default boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		return false;
	}

	static void markHighlightsDirty() {
		SkyblockerMod.getInstance().containerSolverManager.markDirty();
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
