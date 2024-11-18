package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SkyblockIdTooltip extends SimpleTooltipAdder {

	public SkyblockIdTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		String skyblockId = stack.getSkyblockId();

		if (!skyblockId.isEmpty()) {
			lines.add(Text.literal("Skyblock ID: " + skyblockId).formatted(Formatting.DARK_GRAY));
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableSkyblockId;
	}
}
