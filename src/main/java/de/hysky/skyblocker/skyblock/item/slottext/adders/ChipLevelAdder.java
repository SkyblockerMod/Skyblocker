package de.hysky.skyblocker.skyblock.item.slottext.adders;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ChipLevelAdder extends HeartOfTheXAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"chip_level",
			"skyblocker.config.uiAndVisuals.slotText.chipLevel",
			"skyblocker.config.uiAndVisuals.slotText.chipLevel.@tooltip"
	);

	public ChipLevelAdder() {
		super("^Manage Chips$", CONFIG_INFORMATION);
	}

	@Override
	protected Item getNonLeveledItem() {
		return Items.GRAY_DYE;
	}
}
