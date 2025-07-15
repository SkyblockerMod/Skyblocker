package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;


public class BasicMenu extends RadialMenu {
	final String title;
	final String id;

	public BasicMenu(String title, String id) {
		this.title = title;
		this.id = id;
	}

	public Text getTitle(Text title) {
		return title;
	}

	public boolean titleMatches(String title) {
		return this.getEnabled() && title.equalsIgnoreCase(this.title);
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}

	public String getConfigId() {
		return id;
	}
}
