package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class AvgBinTooltip extends TooltipAdder {
	public AvgBinTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(List<Text> lines, Slot focusedSlot) {
		final ItemStack itemStack = focusedSlot.getStack();
		String neuName = itemStack.getNeuName();
		String internalID = itemStack.getSkyblockId();
		if (neuName == null || internalID == null) return;

		if (SkyblockerConfigManager.get().general.itemTooltip.enableAvgBIN) {
			if (TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null) {
				ItemTooltip.nullWarning();
			} else {
                /*
                  We are skipping check average prices for potions, runes
                  and enchanted books because there is no data for their in API.
                 */
				if (!neuName.isEmpty() && LBinTooltip.lbinExist) {
					GeneralConfig.Average type = ItemTooltip.config.avg;

					// "No data" line because of API not keeping old data, it causes NullPointerException
					if (type == GeneralConfig.Average.ONE_DAY || type == GeneralConfig.Average.BOTH) {
						lines.add(
								Text.literal(String.format("%-19s", "1 Day Avg. Price:"))
								    .formatted(Formatting.GOLD)
								    .append(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName) == null
										    ? Text.literal("No data").formatted(Formatting.RED)
										    : ItemTooltip.getCoinsMessage(TooltipInfoType.ONE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), itemStack.getCount())
								    )
						);
					}
					if (type == GeneralConfig.Average.THREE_DAY || type == GeneralConfig.Average.BOTH) {
						lines.add(
								Text.literal(String.format("%-19s", "3 Day Avg. Price:"))
								    .formatted(Formatting.GOLD)
								    .append(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName) == null
										    ? Text.literal("No data").formatted(Formatting.RED)
										    : ItemTooltip.getCoinsMessage(TooltipInfoType.THREE_DAY_AVERAGE.getData().get(neuName).getAsDouble(), itemStack.getCount())
								    )
						);
					}
				}
			}
		}
	}
}
