package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NpcPriceTooltip extends SimpleTooltipAdder {

	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.NPC.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		// NPC prices seem to use the Skyblock item id, not the Skyblock api id.
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.NPC.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}
		double price = TooltipInfoType.NPC.getData().getOrDefault(internalID, -1); // The original default return value of 0 can be an actual price, so we use a value that can't be a price
		if (price < 0) return;

		int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreString()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(stack.getCount())), 1);

		lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
					.formatted(Formatting.YELLOW)
					.append(ItemTooltip.getCoinsMessage(price, count)));
	}
}
