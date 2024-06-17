package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccessoryTooltip extends TooltipAdder {
	public AccessoryTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.ACCESSORIES.isTooltipEnabledAndHasOrNullWarning(internalID)) {
			Pair<AccessoriesHelper.AccessoryReport, String> report = AccessoriesHelper.calculateReport4Accessory(internalID);

			if (report.left() != AccessoriesHelper.AccessoryReport.INELIGIBLE) {
				lines.add(Text.empty()
				              .append(Text.literal("@align(100)"))
				              .append(Text.literal("Accessory:").withColor(0xf57542)));

				lines.add(switch (report.left()) {
					case HAS_HIGHEST_TIER -> Text.literal("✔ Collected").formatted(Formatting.GREEN);
					case IS_GREATER_TIER -> Text.literal("✦ Upgrade ").withColor(0x218bff).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case HAS_GREATER_TIER -> Text.literal("↑ Upgradable ").withColor(0xf8d048).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case OWNS_BETTER_TIER -> Text.literal("↓ Downgrade ").formatted(Formatting.GRAY).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case MISSING -> Text.literal("✖ Missing ").formatted(Formatting.RED).append(Text.literal(report.right()).withColor(0xf8f8ff));

					//Should never be the case
					default -> Text.literal("? Unknown").formatted(Formatting.GRAY);
				});
			}
		}
	}
}
