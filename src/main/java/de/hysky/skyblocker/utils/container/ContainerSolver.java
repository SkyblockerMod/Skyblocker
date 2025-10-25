package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;

/**
 * A solver for a container without the inventory slots included.
 *
 * @see ContainerAndInventorySolver
 */
public interface ContainerSolver extends ContainerMatcher, Resettable {
	List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots);

	default void start(GenericContainerScreen screen) {}

	@Override
	default void reset() {}

	/**
	 * Called upon marking highlights dirty in {@link ContainerSolverManager#markHighlightsDirty()}.
	 */
	default void markDirty() {}

	default boolean isSolverSlot(Slot slot, Screen screen) {
		if (this instanceof ContainerAndInventorySolver) return true;
		if (screen instanceof GenericContainerScreen generic) {
			return slot.id < generic.getScreenHandler().getRows() * 9;
		}
		assert MinecraftClient.getInstance().player != null;
		return slot.inventory != MinecraftClient.getInstance().player.getInventory();
	}

	/**
	 * Called when the slot is clicked.
	 *
	 * @return {@link SlotClickResult#CANCEL} if the click should be blocked.<br>
	 * {@link SlotClickResult#ALLOW} if it shouldn't.<br>
	 * {@link SlotClickResult#ALLOW_MIDDLE_CLICK} if the left click should be turned into a middle click (ignored if {@code button} isn't {@code 0}).
	 */
	default SlotClickResult onClickSlot(int slot, ItemStack stack, int screenId, int button) {
		return SlotClickResult.ALLOW;
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

	enum SlotClickResult {
		CANCEL,
		ALLOW,
		ALLOW_MIDDLE_CLICK
	}
}
