package de.hysky.skyblocker.skyblock.item.slottext.adders;

import net.minecraft.core.component.DataComponents;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class HotfPerkLevelAdder extends HeartOfTheXAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hotf_perk_level",
			"skyblocker.config.uiAndVisuals.slotText.hotfPerkLevel");

	public HotfPerkLevelAdder() {
		super("^Heart of the Forest$", CONFIG_INFORMATION);
	}

	@Override
	protected boolean isNonLeveledItem(ItemStack stack) {
		Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);

		return BlockItemIds.PALE_OAK_BUTTON.item().identifier().equals(itemModel);
	}
}
