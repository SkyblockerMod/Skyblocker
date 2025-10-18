package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LBinTooltip extends SimpleTooltipAdder {
    public LBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.LOWEST_BINS.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        String skyblockApiId = stack.getSkyblockApiId();

		// Check for whether the item exist in bazaar price data, because Skytils keeps some bazaar item data in lbin api
		if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId) && !TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
			lines.add(Text.literal(String.format("%-19s", "Lowest BIN Price:"))
			              .formatted(Formatting.GOLD)
			              .append(ItemTooltip.getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().getDouble(skyblockApiId), stack.getCount())));
		}
	}
}
