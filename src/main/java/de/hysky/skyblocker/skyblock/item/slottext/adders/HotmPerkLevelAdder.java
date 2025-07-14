package de.hysky.skyblocker.skyblock.item.slottext.adders;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class HotmPerkLevelAdder extends HeartOfTheXAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hotm_perk_level",
			"skyblocker.config.uiAndVisuals.slotText.hotmPerkLevel");

	public HotmPerkLevelAdder() {
		super("^Heart of the Mountain$", CONFIG_INFORMATION);
	}

	@Override
	protected Item getNonLeveledItem() {
		return Items.COAL;
	}
}
