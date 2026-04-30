package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.configs.GeneralConfig.Average;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AvgBinTooltip extends SimpleTooltipAdder {
	public AvgBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		String skyblockApiId = stack.getSkyblockApiId();
		Average type = ItemTooltip.config.get().avg;

		if ((TooltipInfoType.ONE_DAY_AVERAGE.getData() == null && type != Average.THREE_DAY) || (TooltipInfoType.THREE_DAY_AVERAGE.getData() == null && type != Average.ONE_DAY)) {
			ItemTooltip.nullWarning();
		} else {
			// "No data" line because of API not keeping old data, it causes NullPointerException
			if ((type == Average.ONE_DAY || type == Average.BOTH) && TooltipInfoType.ONE_DAY_AVERAGE.hasOrNullWarning(skyblockApiId)) {
				lines.add(Component.literal(String.format("%-19s", "1 Day Avg. Price:"))
						.withStyle(ChatFormatting.GOLD)
						.append(ItemTooltip.getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.getData().getDouble(skyblockApiId), stack.getCount())));
			}
			if ((type == Average.THREE_DAY || type == Average.BOTH) && TooltipInfoType.THREE_DAY_AVERAGE.hasOrNullWarning(skyblockApiId)) {
				lines.add(Component.literal(String.format("%-19s", "3 Day Avg. Price:"))
						.withStyle(ChatFormatting.GOLD)
						.append(ItemTooltip.getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.getData().getDouble(skyblockApiId), stack.getCount())));
			}

		}
	}

	@Override
	public boolean isEnabled() {
		//Both 1 day and 3 day averages use the same config option, so we only need to check one
		return TooltipInfoType.THREE_DAY_AVERAGE.isTooltipEnabled();
	}
}
