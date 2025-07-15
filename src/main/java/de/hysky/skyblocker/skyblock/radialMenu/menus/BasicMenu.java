package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;


public class BasicMenu implements RadialMenu {
	final String title;

	public BasicMenu(String title) {
		this.title = title;
	}

	@Override
	public Text getTitle(Text title) {
		return title;
	}

	@Override
	public boolean titleMatches(String title) {
		return this.isEnabled() && title.equalsIgnoreCase(this.title);
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
