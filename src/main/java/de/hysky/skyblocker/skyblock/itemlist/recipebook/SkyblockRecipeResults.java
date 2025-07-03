package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
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
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

//TODO when in recipe view set search hint to talk about close or smth
/**
 * Based off {@link net.minecraft.client.gui.screen.recipebook.RecipeBookResults}.
 */
public class SkyblockRecipeResults implements RecipeAreaDisplay {
	/**
	 * The width before text will go outside of the recipe book area.
	 */
	private static final int MAX_TEXT_WIDTH = 124;
	private static final String ELLIPSIS_STRING = ScreenTexts.ELLIPSIS.getString();

	private final List<SkyblockRecipeResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
	private final List<SkyblockRecipeResultButton> recipeSlotButtons = Lists.newArrayListWithCapacity(16);
	private @Nullable ItemStack recipeIcon = null;
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
	private List<SkyblockRecipe> recipeResults = new ArrayList<>();
	private int pageCount = 0;
	private int currentPage = 0;
	/**
	 * Whether we are showing the recipe for an item (true) or just showing the search results (false).
	 */
	private boolean recipeView = false;

	protected SkyblockRecipeResults() {
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
		updateResultButtons();
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

		for (SkyblockRecipeResultButton resultButton : recipeView ? recipeSlotButtons : resultButtons) {
			resultButton.render(context, mouseX, mouseY, delta);

			if (resultButton.visible && resultButton.isSelected()) this.hoveredResultButton = resultButton;
		}



		//Render the page flip buttons
		if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
		if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);
	}

	//TODO enable scissor?
	private void drawRecipeDisplay(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
		SkyblockRecipe recipe = this.recipeResults.get(this.currentPage);
		//Render the "Craft Text" which is usually a requirement (e.g. Wolf Slayer 7)
		String craftText = recipe.getExtraText().getString();

		if (!craftText.isEmpty()) {
			if (textRenderer.getWidth(craftText) > MAX_TEXT_WIDTH) {
				//Set craft text as hovered text if we're hovering over it since it got truncated
				if (isMouseHoveringText(x + 11, y + 31, mouseX, mouseY)) this.hoveredText = Text.literal(craftText);

				craftText = textRenderer.trimToWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS_STRING;
			}

			context.drawTextWithShadow(textRenderer, craftText, x + 11, y + 31, 0xffffffff);
		}

		//Render the resulting item's name
		Text itemName = recipe.getOutputs().getFirst().getName();

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
		if (this.hoveredText == null && mouseX >= x + 86 && mouseY >= y + 81 && mouseX < x + 86 + 25 && mouseY < y + 81 + 25 && recipe instanceof SkyblockForgeRecipe forgeRecipe) {
			this.hoveredText = Text.of(forgeRecipe.getDurationString());
		}
		if (recipeIcon != null) context.drawItem(recipeIcon, x + 115, y + 61);
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

				List<Text> lore = Screen.getTooltipFromItem(MinecraftClient.getInstance(), stack);

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
			SkyblockRecipe recipe = this.recipeResults.get(this.currentPage);

			//Clear all result buttons to make way for displaying the recipe
			for (SkyblockRecipeResultButton button : this.resultButtons) {
				button.clearDisplayStack();
			}
			recipeSlotButtons.clear();

			//Put the recipe in the proper result buttons

			switch (recipe) {
				case SkyblockCraftingRecipe craftingRecipe -> {
					recipeIcon = new ItemStack(Items.CRAFTING_TABLE);
					//Row 1
					recipeSlotButtons.add(this.resultButtons.get(5).setDisplayStack(craftingRecipe.getGrid().getFirst()));
					recipeSlotButtons.add(this.resultButtons.get(6).setDisplayStack(craftingRecipe.getGrid().get(1)));
					recipeSlotButtons.add(this.resultButtons.get(7).setDisplayStack(craftingRecipe.getGrid().get(2)));
					//Row 2
					recipeSlotButtons.add(this.resultButtons.get(10).setDisplayStack(craftingRecipe.getGrid().get(3)));
					recipeSlotButtons.add(this.resultButtons.get(11).setDisplayStack(craftingRecipe.getGrid().get(4)));
					recipeSlotButtons.add(this.resultButtons.get(12).setDisplayStack(craftingRecipe.getGrid().get(5)));
					//Row 3
					recipeSlotButtons.add(this.resultButtons.get(15).setDisplayStack(craftingRecipe.getGrid().get(6)));
					recipeSlotButtons.add(this.resultButtons.get(16).setDisplayStack(craftingRecipe.getGrid().get(7)));
					recipeSlotButtons.add(this.resultButtons.get(17).setDisplayStack(craftingRecipe.getGrid().get(8)));
					//Result
					recipeSlotButtons.add(this.resultButtons.get(14).setDisplayStack(craftingRecipe.getResult()));
				}
				case SkyblockForgeRecipe forgeRecipe -> {

					recipeIcon = new ItemStack(Items.LAVA_BUCKET);


					Vector2i gridSize = forgeRecipe.getGridSize();
					// Using this slot as a center cuz I said so
					SkyblockRecipeResultButton button = this.resultButtons.get(11);
					int startX = button.getX() + button.getWidth() / 2 - (gridSize.x * 25) / 2;
					int startY = button.getY() + button.getHeight() / 2 - (gridSize.y * 25)/2;
					for (int i = 0; i < forgeRecipe.getInputs().size(); i++) {
						int x = startX + (i % gridSize.x) * 25;
						int y = startY + (i / gridSize.x) * 25;
						recipeSlotButtons.add(new SkyblockRecipeResultButton(x, y).setDisplayStack(forgeRecipe.getInputs().get(i)));
					}
					//Result
					recipeSlotButtons.add(this.resultButtons.get(14).setDisplayStack(forgeRecipe.getResult()));
				}
				case null, default -> {}
			}
		} else {
			recipeIcon = null;
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

		for (SkyblockRecipeResultButton resultButton : recipeView ? recipeSlotButtons : this.resultButtons) {
			//If the result button was clicked then try and show a recipe if there is one
			//for the item
			if (resultButton.mouseClicked(mouseX, mouseY, button)) {
				String itemId = ItemUtils.getItemId(resultButton.getDisplayStack());

				//Continue if this item doesn't have an item id
				if (itemId.isEmpty()) continue;

				List<SkyblockRecipe> recipes = ItemRepository.getRecipesAndUsages(resultButton.getDisplayStack());

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
		if (SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) {
			boolean officialWikiLookup = WikiLookup.officialWikiLookup.matchesKey(keyCode, scanCode);
			if (officialWikiLookup || WikiLookup.fandomWikiLookup.matchesKey(keyCode, scanCode)) {
				return this.resultButtons.stream()
						.filter(button -> button.isMouseOver(mouseX, mouseY))
						.findFirst().map(button -> {
							WikiLookup.openWiki(button.getDisplayStack(), client.player, officialWikiLookup);
							return true;
						}).orElse(false);
			}
		}
		return false;
	}
}
