package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class HuntingToolkitIndicatorAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hunting_toolkit_indicator",
			"skyblocker.config.uiAndVisuals.slotText.huntingToolkitIndicator",
			"skyblocker.config.uiAndVisuals.slotText.huntingToolkitIndicator.@Tooltip");

	public HuntingToolkitIndicatorAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		String joinedTooltip = String.join("", stack.skyblocker$getLoreStrings());
		if (joinedTooltip.contains("Part of the Hunting Toolkit!")) {
			return SlotText.topLeftList(Component.literal("❒").withColor(0xFFFF5555));
		}

		return List.of();
	}
}
