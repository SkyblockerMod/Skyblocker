package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Storage extends BasicMenu {
	public Storage() {
		super("Storage", "storage");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		if (item.equals(Items.BLACK_STAINED_GLASS_PANE) || item.equals(Items.RED_STAINED_GLASS_PANE) || item.equals(Items.BROWN_STAINED_GLASS_PANE)) {
			return false;
		}
		return slotId != 4 && slotId != 22;
	}

	@Override
	public String getConfigId() {
		return "storage";
	}
}
