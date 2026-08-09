package de.hysky.skyblocker.skyblock.item.slottext.adders;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class HotfPerkLevelAdder extends HeartOfTheXAdder {
	private static final Identifier PALE_OAK_BUTTON = Identifier.withDefaultNamespace("pale_oak_button");
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"hotf_perk_level",
			"skyblocker.config.uiAndVisuals.slotText.hotfPerkLevel");

	public HotfPerkLevelAdder() {
		super("^Heart of the Forest$", CONFIG_INFORMATION);
	}

	@Override
	protected boolean isNonLeveledItem(ItemStack stack) {
		Identifier itemModel = stack.get(DataComponents.ITEM_MODEL);

		return PALE_OAK_BUTTON.equals(itemModel);
	}
}
