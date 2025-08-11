package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.configs.GeneralConfig.Average;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AvgBinTooltip extends SimpleTooltipAdder {
	public AvgBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String skyblockApiId = stack.getSkyblockApiId();
		String neuName = stack.getNeuName();
		Average type = ItemTooltip.config.avg;

        if ((TooltipInfoType.ONE_DAY_AVERAGE.getData() == null && type != Average.THREE_DAY) || (TooltipInfoType.THREE_DAY_AVERAGE.getData() == null && type != Average.ONE_DAY)) {
			ItemTooltip.nullWarning();
		} else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
			if (!neuName.isEmpty() && TooltipInfoType.LOWEST_BINS.hasOrNullWarning(skyblockApiId)) {

				// "No data" line because of API not keeping old data, it causes NullPointerException
				if (type == Average.ONE_DAY || type == Average.BOTH) {
					lines.add(
							Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
							    .formatted(Formatting.GOLD)
							    .append(!TooltipInfoType.ONE_DAY_AVERAGE.getData().containsKey(neuName)
									    ? Text.literal("No data").formatted(Formatting.RED)
									    : ItemTooltip.getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.getData().getDouble(neuName), stack.getCount())
							    )
					);
				}
				if (type == Average.THREE_DAY || type == Average.BOTH) {
					lines.add(
							Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
							    .formatted(Formatting.GOLD)
							    .append(!TooltipInfoType.THREE_DAY_AVERAGE.getData().containsKey(neuName)
									    ? Text.literal("No data").formatted(Formatting.RED)
									    : ItemTooltip.getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.getData().getDouble(neuName), stack.getCount())
							    )
					);
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		//Both 1 day and 3 day averages use the same config option, so we only need to check one
		return TooltipInfoType.THREE_DAY_AVERAGE.isTooltipEnabled();
	}
}
