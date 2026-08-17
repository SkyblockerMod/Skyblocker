package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Storage extends BasicMenu {
	public Storage() {
		super("Storage", "storage");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		if (item.equals(Items.STAINED_GLASS_PANE.black()) || item.equals(Items.STAINED_GLASS_PANE.red()) || item.equals(Items.STAINED_GLASS_PANE.brown())) {
			return false;
		}
		return slotId != 4 && slotId != 22;
	}

	@Override
	public String getConfigId() {
		return "storage";
	}
}
