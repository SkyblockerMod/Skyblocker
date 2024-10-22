package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DungeonQualityTooltip extends SimpleTooltipAdder {
	public DungeonQualityTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		NbtCompound customData = ItemUtils.getCustomData(stack);
		if (customData.isEmpty() || !customData.contains("baseStatBoostPercentage")) return;
		int baseStatBoostPercentage = customData.getInt("baseStatBoostPercentage");
		boolean maxQuality = baseStatBoostPercentage == 50;
		lines.add(Text.literal("Item Quality:").formatted(Formatting.BLUE)
		              .align(maxQuality
		                     ? Text.literal(baseStatBoostPercentage + "/50")
		                           .formatted(Formatting.RED, Formatting.BOLD)
		                     : Text.literal(baseStatBoostPercentage + "/50")
		                           .formatted(Formatting.BLUE),
				              100));

		if (customData.contains("item_tier")) {     // sometimes it just isn't here?
			int itemTier = customData.getInt("item_tier");
			lines.add(Text.literal("Floor Tier:").formatted(Formatting.BLUE)
			              .align(maxQuality
			                     ? Text.literal(itemTier + " (" + getItemTierFloor(itemTier) + ")")
			                           .formatted(Formatting.RED, Formatting.BOLD)
			                     : Text.literal(itemTier + " (" + getItemTierFloor(itemTier) + ")")
			                           .formatted(Formatting.BLUE),
					              100));
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
