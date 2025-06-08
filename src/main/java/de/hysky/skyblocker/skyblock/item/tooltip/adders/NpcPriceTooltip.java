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
import java.util.OptionalInt;

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

		OptionalInt optCount = ItemUtils.getItemCountInSack(stack, lines);
		// This clamp is here to ensure that the tooltip doesn't show a useless price of 0 coins if the item count is 0.
		int count = optCount.isPresent() ? Math.max(optCount.getAsInt(), 1) : stack.getCount();

		lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
					  .formatted(Formatting.YELLOW)
					  .append(ItemTooltip.getCoinsMessage(price, count)));
	}
}
