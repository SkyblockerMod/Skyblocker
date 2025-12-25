package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import net.azureaaron.networth.NetworthResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EstimatedItemValueTooltip extends SimpleTooltipAdder {

	public EstimatedItemValueTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (TooltipInfoType.BAZAAR.getData() != null && TooltipInfoType.BAZAAR.getData().containsKey(stack.getSkyblockApiId())) {
			return; // Bazaar price already displayed
		}

		int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreStrings()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(stack.getCount())), 1);

		NetworthResult result = NetworthCalculator.getItemNetworth(stack, count);

		if (result.price() > 0) {
			lines.add(Component.literal(String.format("%-20s", "Est. Item Value:"))
					.withStyle(ChatFormatting.GOLD)
					.append(ItemTooltip.getCoinsMessage(result.price(), count, true)));
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.ESTIMATED_ITEM_VALUE.isTooltipEnabled();
	}
}
