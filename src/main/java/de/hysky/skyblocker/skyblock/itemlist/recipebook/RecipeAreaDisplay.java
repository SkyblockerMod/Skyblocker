package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Interface representing the methods needed to interact with the "display" part of a tab in the Skyblock recipe book.
 */
public interface RecipeAreaDisplay {
	void initialize(Minecraft client, int parentLeft, int parentTop);

	void draw(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta);

	void drawTooltip(GuiGraphics context, int x, int y);

	boolean mouseClicked(MouseButtonEvent click, boolean doubled);

	default boolean keyPressed(double mouseX, double mouseY, KeyEvent keyInput) {
		return false;
	}

	default void updateSearchResults(String query, FilterOption filterOption) {
		updateSearchResults(query, filterOption, false);
	}

	/**
	 * If this tab does not use the search bar then no-op this.
	 */
	void updateSearchResults(String query, FilterOption filterOption, boolean refresh);
}
