package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class NpcPriceTooltip extends TooltipAdder {
	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lines, Slot focusedSlot) {
		final ItemStack stack = focusedSlot.getStack();
		final String internalID = stack.getSkyblockId();
		if (internalID != null && TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
			              .formatted(Formatting.YELLOW)
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.getData().get(internalID).getAsDouble(), stack.getCount())));
		}
	}
}
