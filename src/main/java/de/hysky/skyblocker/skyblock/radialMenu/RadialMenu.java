package de.hysky.skyblocker.skyblock.radialMenu;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public interface RadialMenu {
	/**
	 * @param title the original window title
	 * @return  the title show in the radial menu
	 */
	Text getTitle(Text title);

	/**
	 * Works out if this radial menu should be used for this screen title
	 * @param title screen title to compare
	 * @return the menu should be used
	 */
	boolean titleMatches(String title);

	/**
	 * Works out if an item should be added to the menu
	 * @param slotId the slot the items in
	 * @param stack the item
	 * @return if it should be added
	 */
	boolean itemMatches(int slotId, ItemStack stack);


	boolean isEnabled();
}
