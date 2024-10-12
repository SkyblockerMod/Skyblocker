package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.item.ItemStack;

public interface RecipeTab extends RecipeAreaDisplay {
	ItemStack icon();

	/**
	 * If this tab does not use the search bar then no-op this.
	 */
	void initializeSearchResults(String query);
}
