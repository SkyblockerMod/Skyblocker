package de.hysky.skyblocker.utils.container;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;

import org.jspecify.annotations.Nullable;

public interface TooltipAdder extends ContainerMatcher {
	/**
	 * @implNote The first element of the lines list holds the item's display name,
	 * as it's a list of all lines that will be displayed in the tooltip.
	 */
	void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines);

	int getPriority();
}
