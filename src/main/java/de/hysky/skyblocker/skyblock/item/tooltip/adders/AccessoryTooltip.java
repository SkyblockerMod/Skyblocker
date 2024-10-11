package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AccessoryTooltip extends SimpleTooltipAdder {
	public AccessoryTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.ACCESSORIES.hasOrNullWarning(internalID)) {
			Pair<AccessoriesHelper.AccessoryReport, String> report = AccessoriesHelper.calculateReport4Accessory(internalID);

			if (report.left() != AccessoriesHelper.AccessoryReport.INELIGIBLE) {
				MutableText title = Text.literal(String.format("%-19s", "Accessory: ")).withColor(0xf57542);

				Text stateText = switch (report.left()) {
					case HAS_HIGHEST_TIER -> Text.literal("✔ Collected").formatted(Formatting.GREEN);
					case IS_GREATER_TIER -> Text.literal("✦ Upgrade ").withColor(0x218bff).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case HAS_GREATER_TIER -> Text.literal("↑ Upgradable ").withColor(0xf8d048).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case OWNS_BETTER_TIER -> Text.literal("↓ Downgrade ").formatted(Formatting.GRAY).append(Text.literal(report.right()).withColor(0xf8f8ff));
					case MISSING -> Text.literal("✖ Missing ").formatted(Formatting.RED).append(Text.literal(report.right()).withColor(0xf8f8ff));

					//Should never be the case
					default -> Text.literal("? Unknown").formatted(Formatting.GRAY);
				};

				lines.add(title.append(stateText));
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return TooltipInfoType.ACCESSORIES.isTooltipEnabled();
	}
}
