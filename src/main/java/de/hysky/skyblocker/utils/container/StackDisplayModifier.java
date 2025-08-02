package de.hysky.skyblocker.utils.container;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Optional interface for container solvers that want to modify how item stacks
 * are displayed during rendering.
 */
public interface StackDisplayModifier {
	/**
	 * Modifies the stack displayed for a given slot.
	 * Return {@link ItemStack#EMPTY} to display an empty slot.
	 *
	 * @param slotIndex The slot index being rendered
	 * @param stack     The original {@link ItemStack}
	 * @return The stack to display
	 */
	ItemStack modifyDisplayStack(int slotIndex, @NotNull ItemStack stack);
}
