package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class HuntingMenu extends RadialMenu {

	public Text getTitle(Text title) {
		return Text.of("Hunting Toolkit");
	}

	public boolean titleMatches(String title) {
		return this.getEnabled() && title.equalsIgnoreCase("hunting toolkit ➜ selection");
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		if (slotId == 41) return false;
		return !stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE) && !stack.getItem().equals(Items.GRAY_DYE);
	}

	public String getConfigId() {
		return "huntingToolkit";
	}


}
