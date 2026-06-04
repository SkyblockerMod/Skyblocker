package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class LBinTooltip extends SimpleTooltipAdder {
	public LBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.LOWEST_BINS.isTooltipEnabled();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String skyblockApiId = stack.getSkyblockApiId();

		// Check for whether the item exist in bazaar price data, because Skytils keeps some bazaar item data in lbin api
		if (TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId) && !TooltipInfoType.BAZAAR.hasOrNullWarning(skyblockApiId)) {
			lines.add(Component.literal(String.format("%-19s", "Lowest BIN Price:"))
						.withStyle(ChatFormatting.GOLD)
						.append(ItemTooltip.getCoinsMessage(TooltipInfoType.LOWEST_BINS.getData().getDouble(skyblockApiId), stack.getCount())));
		}
	}
}
