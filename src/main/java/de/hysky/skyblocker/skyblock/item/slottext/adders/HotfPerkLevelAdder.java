package de.hysky.skyblocker.skyblock.item.slottext.adders;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class HotfPerkLevelAdder extends HeartOfTheXAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hotf_perk_level",
			"skyblocker.config.uiAndVisuals.slotText.hotfPerkLevel");

	public HotfPerkLevelAdder() {
		super("^Heart of the Forest$", CONFIG_INFORMATION);
	}

	@Override
	protected Item getNonLeveledItem() {
		return Items.PLAYER_HEAD;
	}
}
