package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Skyblock Crafting Tab which handles the mouse clicks & rendering for the results page and the search field.
 */
record SkyblockCraftingTab(SkyblockRecipeBookWidget recipeBook, ItemStack icon, SkyblockRecipeResults results) implements RecipeTab {
	static final ItemStack CRAFTING_TABLE = new ItemStack(Items.CRAFTING_TABLE);

	@Override
	public void initialize(Minecraft client, int parentLeft, int parentTop) {
		results.initialize(client, parentLeft, parentTop);
	}

	@Override
	public void draw(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
		assert recipeBook.searchBox != null;

		if (ItemRepository.filesImported()) {
			recipeBook.searchBox.render(context, mouseX, mouseY, delta);
			recipeBook.filterOption.render(context, mouseX, mouseY, delta);
			results.draw(context, x, y, mouseX, mouseY, delta);
		} else {
			//68 is from 137 / 2 and 137 is the height from which the page flip buttons are rendered
			context.drawCenteredString(Minecraft.getInstance().font, "Loading...", x + (SkyblockRecipeBookWidget.IMAGE_WIDTH / 2), y + 68, 0xFFFFFFFF);
		}
	}

	@Override
	public void drawTooltip(GuiGraphics context, int x, int y) {
		if (ItemRepository.filesImported()) results.drawTooltip(context, x, y);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (ItemRepository.filesImported()) {
			if (results.mouseClicked(click, doubled)) {
				return true;
			} else {
				if (recipeBook.searchBox != null) {
					boolean magnifyingGlassClicked = recipeBook.magnifierIconPlacement != null && recipeBook.magnifierIconPlacement.containsPoint(Mth.floor(click.x()), Mth.floor(click.y()));

					if (magnifyingGlassClicked || recipeBook.searchBox.mouseClicked(click, doubled)) {
						results.closeRecipeView();
						recipeBook.searchBox.setFocused(true);

						return true;
					}
					recipeBook.searchBox.setFocused(false);
					return recipeBook.filterOption.mouseClicked(click, doubled);
				}
			}
		}

		return false;
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, KeyEvent input) {
		return ItemRepository.filesImported() && this.results.keyPressed(mouseX, mouseY, input);
	}

	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {
		if (ItemRepository.filesImported()) results.updateSearchResults(query, filterOption, refresh);
	}
}
