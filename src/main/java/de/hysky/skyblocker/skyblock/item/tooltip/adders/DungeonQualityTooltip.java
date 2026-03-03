package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DungeonQualityTooltip extends SimpleTooltipAdder {
	public DungeonQualityTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		CompoundTag customData = ItemUtils.getCustomData(stack);
		if (customData == null || !customData.contains("baseStatBoostPercentage")) return;
		int baseStatBoostPercentage = customData.getIntOr("baseStatBoostPercentage", 0);
		boolean maxQuality = baseStatBoostPercentage == 50;
		if (maxQuality) {
			lines.add(Component.literal(String.format("%-17s", "Item Quality:") + baseStatBoostPercentage + "/50").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
		} else {
			lines.add(Component.literal(String.format("%-21s", "Item Quality:") + baseStatBoostPercentage + "/50").withStyle(ChatFormatting.BLUE));
		}

		if (customData.contains("item_tier")) {     // sometimes it just isn't here?
			int itemTier = customData.getIntOr("item_tier", 0);
			if (maxQuality) {
				lines.add(Component.literal(String.format("%-17s", "Floor Tier:") + itemTier + " (" + getItemTierFloor(itemTier) + ")").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
			} else {
				lines.add(Component.literal(String.format("%-21s", "Floor Tier:") + itemTier + " (" + getItemTierFloor(itemTier) + ")").withStyle(ChatFormatting.BLUE));
			}
		}
	}

	private String getItemTierFloor(int tier) {
		return switch (tier) {
			case 0 -> "E";
			case 1 -> "F1";
			case 2 -> "F2";
			case 3 -> "F3";
			case 4 -> "F4/M1";
			case 5 -> "F5/M2";
			case 6 -> "F6/M3";
			case 7 -> "F7/M4";
			case 8 -> "M5";
			case 9 -> "M6";
			case 10 -> "M7";
			default -> "Unknown";
		};
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.dungeonQuality;
	}
}
