package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;

public final class PaginationWidget extends ImageButton {

	public PaginationWidget(boolean forwards, OnPress onPress) {
		super(0, 0, RecipeBookPage.TURN_PAGE_SPRITE_WIDTH, RecipeBookPage.TURN_PAGE_SPRITE_HEIGHT, forwards ? RecipeBookPage.PAGE_FORWARD_SPRITES : RecipeBookPage.PAGE_BACKWARD_SPRITES, onPress);
	}
}
