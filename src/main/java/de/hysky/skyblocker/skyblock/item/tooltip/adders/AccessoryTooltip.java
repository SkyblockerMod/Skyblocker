package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.accessories.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import org.jspecify.annotations.Nullable;

public class AccessoryTooltip extends SimpleTooltipAdder {
	public static final int COLLECTED_COLOUR = Formatting.GREEN.getColorValue();
	public static final int UPGRADE_COLOUR = 0x218BFF;
	public static final int UPGRADABLE_COLOUR = 0xF8D048;
	public static final int DOWNGRADE_COLOUR = Formatting.GRAY.getColorValue();
	public static final int MISSING_COLOUR = Formatting.RED.getColorValue();

	public AccessoryTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.ACCESSORIES.hasOrNullWarning(internalID)) {
			Pair<AccessoriesHelper.AccessoryReport, String> report = AccessoriesHelper.calculateReport4Accessory(internalID);

			if (report.left() != AccessoriesHelper.AccessoryReport.INELIGIBLE) {
				MutableText title = Text.literal(String.format("%-19s", "Accessory: ")).withColor(0xF57542);

				Text stateText = switch (report.left()) {
					case HAS_HIGHEST_TIER -> Text.literal("✔ Collected").withColor(COLLECTED_COLOUR);
					case IS_GREATER_TIER -> Text.literal("✦ Upgrade ").withColor(UPGRADE_COLOUR).append(Text.literal(report.right()).withColor(0xF8F8FF));
					case HAS_GREATER_TIER -> Text.literal("↑ Upgradable ").withColor(UPGRADABLE_COLOUR).append(Text.literal(report.right()).withColor(0xF8F8FF));
					case OWNS_BETTER_TIER -> Text.literal("↓ Downgrade ").withColor(DOWNGRADE_COLOUR).append(Text.literal(report.right()).withColor(0xF8F8FF));
					case MISSING -> Text.literal("✖ Missing ").withColor(MISSING_COLOUR).append(Text.literal(report.right()).withColor(0xF8F8FF));

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
