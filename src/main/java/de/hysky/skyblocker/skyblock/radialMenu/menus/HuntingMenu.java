package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class HuntingMenu extends BasicMenu {

	public HuntingMenu() {
		super("hunting toolkit ➜ selection", "huntingToolkit");
	}

	@Override
	public Text getTitle(Text title) {
		return Text.of("Hunting Toolkit");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		if (slotId == 41) return false;
		return !stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE) && !stack.getItem().equals(Items.GRAY_DYE);
	}
}
