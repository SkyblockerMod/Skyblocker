package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class LBinTooltip extends TooltipAdder {
	public static boolean lbinExist = false;

	public LBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lines, Slot focusedSlot) {
		lbinExist = false;
		final ItemStack itemStack = focusedSlot.getStack();
		final String internalID = itemStack.getSkyblockId();
		if (internalID == null) return;
		String name = itemStack.getSkyblockName();
		if (name == null) return;

		if (name.startsWith("ISSHINY_")) name = "SHINY_" + internalID;

		// bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api

		if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !BazaarPriceTooltip.bazaarExist) {
			lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
			              .formatted(Formatting.GOLD)
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().get(name).getAsDouble(), itemStack.getCount())));
			lbinExist = true;
		}
	}
}
