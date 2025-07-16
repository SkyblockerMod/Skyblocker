package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WardrobeMenu extends RegexMenu {
	public WardrobeMenu() {
		super("wardrobe .*", "wardrobe");
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		return (item.equals(Items.ARROW) || item.equals(Items.LIME_DYE) || item.equals(Items.PINK_DYE) || item.equals(Items.BARRIER));
	}
}
