package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WardrobeMenu extends RegexMenu {
	public WardrobeMenu() {
		super("wardrobe .*", "wardrobe");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		Item item = stack.getItem();
		if (item.equals(Items.ARROW) || item.equals(Items.BARRIER)) return true;
		return (!item.equals(Items.BLACK_STAINED_GLASS_PANE) && slotId < 9);
	}

	@Override
	public int clickSlotOffset(int slotId) {
		return (slotId < 9) ? 36 : 0;
	}

	@Override
	public String[] getNavigationItemNames() {
		return new String[]{"Go Back", "Close", "Next Page", "Previous Page"};
	}
}
