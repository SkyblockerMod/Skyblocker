package de.hysky.skyblocker.utils.container;

import net.minecraft.item.ItemStack;

/**
 * Optional interface for container solvers that want to modify how item stacks
 * are displayed during rendering.
 */
public interface StackDisplayModifier {
	/**
	 * Allows the solver to modify the stack displayed for a given slot.
	 *
	 * @param slotIndex The slot index being rendered
	 * @param stack     The original {@link ItemStack}
	 * @return The stack to display
	 */
	default ItemStack modifyDisplayStack(int slotIndex, ItemStack stack) {
		return stack;
	}

	/**
	 * Determines whether the original stack should be rendered and shown in a tooltip.
	 *
	 * @param slotIndex The slot index being rendered
	 * @param stack     The current {@link ItemStack}
	 * @return {@code true} if the item should be drawn and tooltiped
	 */
	default boolean shouldDisplayStack(int slotIndex, ItemStack stack) {
		return true;
	}
}
