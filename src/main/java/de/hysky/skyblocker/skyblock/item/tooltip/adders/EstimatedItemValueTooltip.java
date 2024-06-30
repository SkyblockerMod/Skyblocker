package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.NetworthResult;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EstimatedItemValueTooltip extends TooltipAdder {

	public EstimatedItemValueTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (TooltipInfoType.ESTIMATED_ITEM_VALUE.isTooltipEnabled()) {
			NetworthResult result = NetworthCalculator.getItemNetworth(stack);

			if (result.price() > 0) {
				lines.add(Text.literal(String.format("%-20s", "Est. Item Value:"))
						.formatted(Formatting.GOLD)
						.append(ItemTooltip.getCoinsMessage(result.price(), stack.getCount(), true)));
			}
		}
	}
}
