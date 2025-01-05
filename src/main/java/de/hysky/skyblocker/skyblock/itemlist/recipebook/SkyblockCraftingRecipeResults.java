package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

//TODO when in recipe view set search hint to talk about close or smth
/**
 * Based off {@link net.minecraft.client.gui.screen.recipebook.RecipeBookResults}.
 */
public class SkyblockCraftingRecipeResults implements RecipeAreaDisplay {
	/**
	 * The width before text will go outside of the recipe book area.
	 */
	private static final int MAX_TEXT_WIDTH = 124;
	private static final String ELLIPSIS_STRING = ScreenTexts.ELLIPSIS.getString();

	private final List<SkyblockRecipeResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
	private MinecraftClient client;
	private ToggleButtonWidget nextPageButton;
	private ToggleButtonWidget prevPageButton;
	private SkyblockRecipeResultButton hoveredResultButton;
	private String lastSearchQuery = null;
	private final List<ItemStack> searchResults = new ArrayList<>();
	/**
	 * Text to be displayed as a tooltip.
	 */
	private Text hoveredText;
	private List<SkyblockCraftingRecipe> recipeResults = new ArrayList<>();
	private int pageCount = 0;
	private int currentPage = 0;
	/**
	 * Whether we are showing the recipe for an item (true) or just showing the search results (false).
	 */
	private boolean recipeView = false;

	protected SkyblockCraftingRecipeResults() {
		for (int i = 0; i < 20; i++) {
			this.resultButtons.add(new SkyblockRecipeResultButton());
		}
	}

	@Override
	public void initialize(MinecraftClient client, int parentLeft, int parentTop) {
		this.client = client;

		//Position the result buttons
		for (int i = 0; i < resultButtons.size(); i++) {
			this.resultButtons.get(i).setPosition(parentLeft + 11 + 25 * (i % 5), parentTop + 31 + 25 * (i / 5));
		}

		//Setup & position the page flip buttons
		this.nextPageButton = new ToggleButtonWidget(parentLeft + 93, parentTop + 137, 12, 17, false);
		this.nextPageButton.setTextures(RecipeBookResults.PAGE_FORWARD_TEXTURES);
		this.prevPageButton = new ToggleButtonWidget(parentLeft + 38, parentTop + 137, 12, 17, true);
		this.prevPageButton.setTextures(RecipeBookResults.PAGE_BACKWARD_TEXTURES);
	}

	@Override
	public void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta) {
		TextRenderer textRenderer = this.client.textRenderer;

		//Reset the hovered text
		this.hoveredText = null;

		//If we have selected an item to view recipes for then show the recipe view specific stuff (e.g. name, requirement)
		if (this.recipeView) {
			drawRecipeDisplay(context, textRenderer, x, y, mouseX, mouseY);
		}

		//Render the page count
		if (this.pageCount > 1) {
			Text text = Text.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
			int width = textRenderer.getWidth(text);

			context.drawText(textRenderer, text, x - width / 2 + 73, y + 141, -1, false);
		}

		//Render the results
		this.hoveredResultButton = null;

		for (SkyblockRecipeResultButton resultButton : resultButtons) {
			resultButton.render(context, mouseX, mouseY, delta);

			if (resultButton.visible && resultButton.isSelected()) this.hoveredResultButton = resultButton;
		}

		//Render the page flip buttons
		if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
		if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);
	}

	//TODO enable scissor?
	private void drawRecipeDisplay(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
		//Render the "Craft Text" which is usually a requirement (e.g. Wolf Slayer 7)
		String craftText = this.recipeResults.get(this.currentPage).getCraftText();

		if (!craftText.isEmpty()) {
			if (textRenderer.getWidth(craftText) > MAX_TEXT_WIDTH) {
				//Set craft text as hovered text if we're hovering over it since it got truncated
				if (isMouseHoveringText(x + 11, y + 31, mouseX, mouseY)) this.hoveredText = Text.literal(craftText);

				craftText = textRenderer.trimToWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS_STRING;
			}

			context.drawTextWithShadow(textRenderer, craftText, x + 11, y + 31, 0xffffffff);
		}

		//Render the resulting item's name
		Text itemName = this.recipeResults.get(this.currentPage).getResult().getName();

		if (textRenderer.getWidth(itemName) > MAX_TEXT_WIDTH) {
			StringVisitable trimmed = StringVisitable.concat(textRenderer.trimToWidth(itemName, MAX_TEXT_WIDTH), ScreenTexts.ELLIPSIS);
			OrderedText ordered = Language.getInstance().reorder(trimmed);

			context.drawTextWithShadow(textRenderer, ordered, x + 11, y + 43, 0xffffffff);

			//Set the resulting item's name as hovered text if we're hovering over it since the text got truncated
			if (isMouseHoveringText(x + 11, y + 43, mouseX, mouseY)) this.hoveredText = itemName;
		} else {
			context.drawTextWithShadow(textRenderer, itemName, x + 11, y + 43, 0xffffffff);
		}

		//Draw the arrow that points to the recipe's result
		context.drawTextWithShadow(textRenderer, "▶", x + 96, y + 90, 0xaaffffff);
	}

	@Override
	public void drawTooltip(DrawContext context, int x, int y) {
		if (this.client.currentScreen != null) {
			//Draw the tooltip of the hovered result button if one is hovered over
			if (this.hoveredResultButton != null && !this.hoveredResultButton.getDisplayStack().isEmpty()) {
				ItemStack stack = this.hoveredResultButton.getDisplayStack();
				Identifier tooltipStyle = stack.get(DataComponentTypes.TOOLTIP_STYLE);

				context.drawTooltip(this.client.textRenderer, SkyblockRecipeResultButton.getTooltip(stack), x, y, tooltipStyle);
			} else if (this.hoveredText != null) {
				//Draw text as a tooltip if it got truncated & we're hovering over it (for recipe display)
				context.drawTooltip(this.client.textRenderer, this.hoveredText, x, y);
			}
		}
	}

	/**
	 * Returns true if the mouse is hovering over the text at this location.
	 */
	private boolean isMouseHoveringText(int textX, int textY, int mouseX, int mouseY) {
		return RenderHelper.pointIsInArea(mouseX, mouseY, textX, textY, textX + MAX_TEXT_WIDTH + 4, textY + this.client.textRenderer.fontHeight);
	}

	protected void closeRecipeView() {
		this.currentPage = 0;
		this.pageCount = (this.searchResults.size() - 1) / resultButtons.size() + 1;
		this.recipeView = false;
		updateResultButtons();
	}

	/**
	 * Handles updating the search results when a character is typed into the search bar,
	 * 
	 * @implNote The {@code query} is always passed as lower case.
	 */
	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {
		if (!ItemRepository.filesImported()) return;
		if (!query.equals(this.lastSearchQuery) || refresh) {
			this.lastSearchQuery = query;
			this.searchResults.clear();

			//Search for stacks which contain the search term
			for (ItemStack stack : ItemRepository.getItems()) {
				String name = stack.getName().getString().toLowerCase(Locale.ENGLISH);
				if (!filterOption.test(name)) continue;
				List<Text> lore = ItemUtils.getLore(stack);

				if (name.contains(query) || lore.stream().map(Text::getString)
						.map(string -> string.toLowerCase(Locale.ENGLISH))
						.anyMatch(line -> line.contains(query))) {
					this.searchResults.add(stack);
				}
			}

			closeRecipeView();
		} else {
			hideShowPageButtons(); //This branch is called when the recipe book is reinitialized (usually from resizing)
		}
	}

	/**
	 * Updates the result buttons.
	 */
	private void updateResultButtons() {
		if (this.recipeView) {
			SkyblockCraftingRecipe recipe = this.recipeResults.get(this.currentPage);

			//Clear all result buttons to make way for displaying the recipe
			for (SkyblockRecipeResultButton button : this.resultButtons) {
				button.clearDisplayStack();
			}

			//Put the recipe in the proper result buttons

			//Row 1
			this.resultButtons.get(5).setDisplayStack(recipe.getGrid().getFirst());
			this.resultButtons.get(6).setDisplayStack(recipe.getGrid().get(1));
			this.resultButtons.get(7).setDisplayStack(recipe.getGrid().get(2));
			//Row 2
			this.resultButtons.get(10).setDisplayStack(recipe.getGrid().get(3));
			this.resultButtons.get(11).setDisplayStack(recipe.getGrid().get(4));
			this.resultButtons.get(12).setDisplayStack(recipe.getGrid().get(5));
			//Row 3
			this.resultButtons.get(15).setDisplayStack(recipe.getGrid().get(6));
			this.resultButtons.get(16).setDisplayStack(recipe.getGrid().get(7));
			this.resultButtons.get(17).setDisplayStack(recipe.getGrid().get(8));
			//Result
			this.resultButtons.get(14).setDisplayStack(recipe.getResult());
		} else {
			//Update the result buttons with the stacks from the search results
			for (int i = 0; i < resultButtons.size(); ++i) {
				int index = this.currentPage * resultButtons.size() + i;

				if (index < this.searchResults.size()) {
					resultButtons.get(i).setDisplayStack(this.searchResults.get(index));
				} else {
					resultButtons.get(i).clearDisplayStack();
				}
			}
		}

		hideShowPageButtons();
	}

	/**
	 * Hides or shows the page buttons.
	 */
	private void hideShowPageButtons() {
		//Show the previous page button if the page count is greater than 0
		this.prevPageButton.active = this.currentPage > 0;
		//Show the next page button if the current page is less than the highest possible page
		this.nextPageButton.active = this.currentPage < this.pageCount - 1;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
			this.currentPage++;
			this.updateResultButtons();

			return true;
		} else if (this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
			this.currentPage--;
			this.updateResultButtons();

			return true;
		}

		if (this.recipeView && button == 1) {
			// The crafting result button
			var result = resultButtons.get(14);
			var rawID = ItemUtils.getItemId(result.getDisplayStack());
			if (result.isMouseOver(mouseX, mouseY)) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown(String.format("/viewrecipe %s", rawID), true);
				return true;
			}
		}

		for (SkyblockRecipeResultButton resultButton : this.resultButtons) {
			//If the result button was clicked then try and show a recipe if there is one
			//for the item
			if (resultButton.mouseClicked(mouseX, mouseY, button)) {
				String itemId = ItemUtils.getItemId(resultButton.getDisplayStack());

				//Continue if this item doesn't have an item id
				if (itemId.isEmpty()) continue;

				List<SkyblockCraftingRecipe> recipes = ItemRepository.getRecipes(itemId);

				//If this item has recipes then set the fields so that they can be displayed
				if (!recipes.isEmpty()) {
					this.recipeResults = recipes;
					this.currentPage = 0;
					this.pageCount = recipes.size();
					this.recipeView = true;
					this.updateResultButtons();
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
		if (SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup
			&& WikiLookup.wikiLookup.matchesKey(keyCode, scanCode))
			return this.resultButtons.stream()
					.filter(button -> button.isMouseOver(mouseX, mouseY))
					.findFirst().map(button -> {
						WikiLookup.openWiki(button.getDisplayStack(), client.player);
						return true;
					}).orElse(false);
		return false;
	}
}
