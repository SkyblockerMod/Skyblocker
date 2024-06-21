package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
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
		final String internalID = stack.getSkyblockId();
		if (internalID != null && TooltipInfoType.NPC.hasOrNullWarning(internalID)) {
			lines.add(Text.literal(String.format("%-21s", "NPC Sell Price:"))
			              .formatted(Formatting.YELLOW)
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.getData().get(internalID).getAsDouble(), stack.getCount())));
		}
	}
}
