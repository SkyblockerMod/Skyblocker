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

public class LBinTooltip extends TooltipAdder {
	public static boolean lbinExist = false;

	public LBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		lbinExist = false;
		final String internalID = stack.getSkyblockId();
		if (internalID == null) return;
		String name = stack.getSkyblockApiId();
		if (name == null) return;

		if (name.startsWith("ISSHINY_")) name = "SHINY_" + internalID;

		// bazaarOpened & bazaarExist check for lbin, because Skytils keeps some bazaar item data in lbin api

		if (TooltipInfoType.LOWEST_BINS.isTooltipEnabledAndHasOrNullWarning(name) && !BazaarPriceTooltip.bazaarExist) {
			lines.add(Text.empty()
			              .append(Text.literal("@align(100)"))
			              .append(Text.literal("Lowest BIN Price:").formatted(Formatting.GOLD)));
			lines.add(Text.empty()
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().get(name).getAsDouble(), stack.getCount())));
			lbinExist = true;
		}
	}
}
