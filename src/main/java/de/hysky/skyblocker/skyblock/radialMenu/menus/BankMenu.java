package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class BankMenu extends RegexMenu {
	public BankMenu() {
		super(".*bank.*", "bank");
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		return !(item.equals(Items.BLACK_STAINED_GLASS_PANE) || item.equals(Items.GOLD_BLOCK) || item.equals(Items.REDSTONE_TORCH) || item.equals(Items.FILLED_MAP));

	}
}
