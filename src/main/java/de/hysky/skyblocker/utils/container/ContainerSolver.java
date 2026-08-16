package de.hysky.skyblocker.utils.container;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;

/// A solver for a container screen.
///
/// For options, see [#skyblockOnly()], [#chestScreensOnly()], and [#chestInventoryOnly()].
public interface ContainerSolver extends ContainerMatcher, Resettable {
	List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots);

	default void start(AbstractContainerScreen<?> screen) {}

	@Override
	default void reset() {}

	/**
	 * Called upon marking highlights dirty in {@link ContainerSolverManager#markHighlightsDirty()}.
	 */
	default void markDirty() {}

	default boolean isSolverSlot(Slot slot, Screen screen) {
		if (!chestInventoryOnly()) return true;
		if (screen instanceof ContainerScreen generic) {
			return slot.index < generic.getMenu().getRowCount() * 9;
		}
		assert Minecraft.getInstance().player != null;
		return slot.container != Minecraft.getInstance().player.getInventory();
	}

	/**
	 * Called when the slot is clicked.
	 *
	 * @return {@code true} if the click should be canceled, {@code false} otherwise. Defaults to {@code false} if not overridden.
	 */
	default boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
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

	/// @return true if this solver should only work in Skyblock.
	default boolean skyblockOnly() {
		return true;
	}

	/// Override and return false to make this solver work in the inventory screen and
	/// other {@link net.minecraft.client.gui.screens.inventory.AbstractContainerScreen AbstractContainerScreen}s.
	default boolean chestScreensOnly() {
		return true;
	}

	/// Override and return false to include the player inventory slots in this solver.
	default boolean chestInventoryOnly() {
		return true;
	}
}
