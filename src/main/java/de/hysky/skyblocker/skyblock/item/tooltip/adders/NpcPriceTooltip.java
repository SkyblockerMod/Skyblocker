package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class NpcPriceTooltip extends SimpleTooltipAdder {

	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.NPC.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		// NPC prices seem to use the Skyblock item id, not the Skyblock api id.
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.NPC.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}
		double price = TooltipInfoType.NPC.getData().getOrDefault(internalID, -1); // The original default return value of 0 can be an actual price, so we use a value that can't be a price
		if (price < 0) return;

		int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreStrings()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(ItemUtils.getCompostCountInComposter(stack.skyblocker$getLoreStrings()).orElse(stack.getCount()))), 1);

		lines.add(Component.literal(String.format("%-21s", "NPC Sell Price:"))
					.withStyle(ChatFormatting.YELLOW)
					.append(ItemTooltip.getCoinsMessage(price, count)));
	}
}
