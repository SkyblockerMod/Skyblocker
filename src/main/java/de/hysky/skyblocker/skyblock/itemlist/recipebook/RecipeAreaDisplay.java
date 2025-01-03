package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Interface representing the methods needed to interact with the "display" part of a tab in the Skyblock recipe book.
 */
public interface RecipeAreaDisplay {
	void initialize(MinecraftClient client, int parentLeft, int parentTop);

	void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta);

	void drawTooltip(DrawContext context, int x, int y);

	boolean mouseClicked(double mouseX, double mouseY, int button);

	default boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
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
