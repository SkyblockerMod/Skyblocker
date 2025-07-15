package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class HuntingMenu implements RadialMenu {

	@Override
	public Text getTitle(Text title) {
		return Text.of("Hunting Toolkit");
	}

	@Override
	public boolean titleMatches(String title) {
		return this.isEnabled() && title.equalsIgnoreCase("hunting toolkit ➜ selection");
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		if (slotId == 41) return false;
		return !stack.getItem().equals(Items.BLACK_STAINED_GLASS_PANE) && !stack.getItem().equals(Items.GRAY_DYE);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
