package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NpcPriceTooltip extends TooltipAdder {
	public NpcPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (internalID != null && TooltipInfoType.NPC.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			lines.add(Text.empty()
			              .append(Text.literal("@align(100)"))
			              .append(Text.literal("NPC Sell Price:").formatted(Formatting.YELLOW)));
			lines.add(Text.empty()
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.NPC.getData().get(internalID).getAsDouble(), stack.getCount())));
		}
	}
}
