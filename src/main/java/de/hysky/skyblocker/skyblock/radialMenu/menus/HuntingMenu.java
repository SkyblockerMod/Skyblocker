package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HuntingMenu extends BasicMenu {

	public HuntingMenu() {
		super("hunting toolkit ➜ selection", "huntingToolkit");
	}

	@Override
	public Component getTitle(Component title) {
		return Component.nullToEmpty("Hunting Toolkit");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		if (slotId == 41) return false;
		return !stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE) && !stack.getItem().equals(Items.GRAY_DYE);
	}
}
