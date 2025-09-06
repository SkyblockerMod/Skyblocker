package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.ItemStack;

public class CollectionsMenu extends RegexMenu {
	public CollectionsMenu() {
		super(".*collections", "collections");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		if (slotId == 4) return false;
		return super.itemMatches(slotId, stack);
	}
}
