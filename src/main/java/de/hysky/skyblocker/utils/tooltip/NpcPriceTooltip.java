package de.hysky.skyblocker.utils.tooltip;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class NpcPriceTooltip extends TooltipAdder {
	protected NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lore, Slot focusedSlot) {
		final ItemStack stack = focusedSlot.getStack();
		final String internalID = ItemTooltip.getInternalNameFromNBT(stack, true);
		if (internalID != null && TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lore.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
			             .formatted(Formatting.YELLOW)
			             .append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.getData().get(internalID).getAsDouble(), stack.getCount())));
		}
	}
}
