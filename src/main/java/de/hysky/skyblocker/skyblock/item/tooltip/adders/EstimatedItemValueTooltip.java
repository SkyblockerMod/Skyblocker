package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.NetworthResult;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EstimatedItemValueTooltip extends SimpleTooltipAdder {

	public EstimatedItemValueTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		NetworthResult result = NetworthCalculator.getItemNetworth(stack);

		if (result.price() > 0) {
			lines.add(Text.literal(String.format("%-20s", "Est. Item Value:"))
					.formatted(Formatting.GOLD)
					.align(ItemTooltip.getCoinsMessage(result.price(), stack.getCount(), true), 100));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.ESTIMATED_ITEM_VALUE.isTooltipEnabled();
	}
}
