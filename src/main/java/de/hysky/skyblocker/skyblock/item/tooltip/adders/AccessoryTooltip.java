package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.skyblock.accessories.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class AccessoryTooltip extends SimpleTooltipAdder {
	public static final int COLLECTED_COLOUR = ChatFormatting.GREEN.getColor();
	public static final int UPGRADE_COLOUR = 0x218BFF;
	public static final int UPGRADABLE_COLOUR = 0xF8D048;
	public static final int DOWNGRADE_COLOUR = ChatFormatting.GRAY.getColor();
	public static final int MISSING_COLOUR = ChatFormatting.RED.getColor();

	public AccessoryTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		final String internalID = stack.getSkyblockId();
		if (TooltipInfoType.ACCESSORIES.hasOrNullWarning(internalID)) {
			Pair<AccessoriesHelper.AccessoryReport, String> report = AccessoriesHelper.calculateReport4Accessory(internalID);

			if (report.left() != AccessoriesHelper.AccessoryReport.INELIGIBLE) {
				MutableComponent title = Component.literal(String.format("%-19s", "Accessory: ")).withColor(0xF57542);

				Component stateText = switch (report.left()) {
					case HAS_HIGHEST_TIER -> Component.literal("✔ Collected").withColor(COLLECTED_COLOUR);
					case IS_GREATER_TIER -> Component.literal("✦ Upgrade ").withColor(UPGRADE_COLOUR).append(Component.literal(report.right()).withColor(0xF8F8FF));
					case HAS_GREATER_TIER -> Component.literal("↑ Upgradable ").withColor(UPGRADABLE_COLOUR).append(Component.literal(report.right()).withColor(0xF8F8FF));
					case OWNS_BETTER_TIER -> Component.literal("↓ Downgrade ").withColor(DOWNGRADE_COLOUR).append(Component.literal(report.right()).withColor(0xF8F8FF));
					case MISSING -> Component.literal("✖ Missing ").withColor(MISSING_COLOUR).append(Component.literal(report.right()).withColor(0xF8F8FF));

					//Should never be the case
					default -> Component.literal("? Unknown").withStyle(ChatFormatting.GRAY);
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
