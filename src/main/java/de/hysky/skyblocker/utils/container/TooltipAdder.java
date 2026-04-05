package de.hysky.skyblocker.utils.container;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface TooltipAdder extends ContainerMatcher {
	/**
	 * @implNote The first element of the lines list holds the item's display name,
	 * as it's a list of all lines that will be displayed in the tooltip.
	 */
	void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines);

	int getPriority();
}
