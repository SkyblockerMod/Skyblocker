package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.world.item.ItemStack;

public interface RecipeTab extends RecipeAreaDisplay {
	int AVAILABLE_WIDTH = 131;
	int AVAILABLE_HEIGHT = 150;
	int EDGE = 9;
	ItemStack icon();
}
