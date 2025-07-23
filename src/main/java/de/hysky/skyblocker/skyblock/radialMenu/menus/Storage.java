package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class Storage extends RadialMenu {
	@Override
	protected Text getTitle(Text title) {
		return title;
	}

	@Override
	protected boolean titleMatches(String title) {
		return title.equalsIgnoreCase("Storage");
	}

	@Override
	protected boolean itemMatches(int slotId, ItemStack stack) {
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
