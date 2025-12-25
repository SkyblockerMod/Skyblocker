package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockNpcShopRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

//TODO when in recipe view set search hint to talk about close or smth
/**
 * Based off {@link net.minecraft.client.gui.screens.recipebook.RecipeBookPage}.
 */
public class SkyblockRecipeResults implements RecipeAreaDisplay {
	/**
	 * The width before text will go outside of the recipe book area.
	 */
	private static final int MAX_TEXT_WIDTH = 124;
	private static final String ELLIPSIS_STRING = CommonComponents.ELLIPSIS.getString();

	private final List<SkyblockRecipeResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
	private final List<SkyblockRecipeResultButton> recipeSlotButtons = Lists.newArrayListWithCapacity(16);
	private @Nullable ItemStack recipeIcon = null;
	private final Minecraft client = Minecraft.getInstance();
	private StateSwitchingButton nextPageButton;
	private StateSwitchingButton prevPageButton;
	private SkyblockRecipeResultButton hoveredResultButton;
	private String lastSearchQuery = null;
	private final List<ItemStack> searchResults = new ArrayList<>();
	/**
	 * Text to be displayed as a tooltip.
	 */
	private Component hoveredText;
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
	public void initialize(Minecraft client, int parentLeft, int parentTop) {
		//Position the result buttons
		for (int i = 0; i < resultButtons.size(); i++) {
			this.resultButtons.get(i).setPosition(parentLeft + 11 + 25 * (i % 5), parentTop + 31 + 25 * (i / 5));
		}

		//Setup & position the page flip buttons
		this.nextPageButton = new StateSwitchingButton(parentLeft + 93, parentTop + 137, 12, 17, false);
		this.nextPageButton.initTextureValues(RecipeBookPage.PAGE_FORWARD_SPRITES);
		this.prevPageButton = new StateSwitchingButton(parentLeft + 38, parentTop + 137, 12, 17, true);
		this.prevPageButton.initTextureValues(RecipeBookPage.PAGE_BACKWARD_SPRITES);
		updateResultButtons();
	}

	@Override
	public void draw(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
		Font textRenderer = this.client.font;

		//Reset the hovered text
		this.hoveredText = null;

		//If we have selected an item to view recipes for then show the recipe view specific stuff (e.g. name, requirement)
		if (this.recipeView) {
			drawRecipeDisplay(context, textRenderer, x, y, mouseX, mouseY);
		}

		//Render the page count
		if (this.pageCount > 1) {
			Component text = Component.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
			int width = textRenderer.width(text);

			context.drawString(textRenderer, text, x - width / 2 + 73, y + 141, CommonColors.WHITE, false);
		}

		//Render the results
		this.hoveredResultButton = null;

		for (SkyblockRecipeResultButton resultButton : recipeView ? recipeSlotButtons : resultButtons) {
			resultButton.render(context, mouseX, mouseY, delta);

			if (resultButton.visible && resultButton.isHoveredOrFocused()) this.hoveredResultButton = resultButton;
		}



		//Render the page flip buttons
		if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
		if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);
	}

	//TODO enable scissor?
	private void drawRecipeDisplay(GuiGraphics context, Font textRenderer, int x, int y, int mouseX, int mouseY) {
		SkyblockRecipe recipe = this.recipeResults.get(this.currentPage);
		//Render the "Craft Text" which is usually a requirement (e.g. Wolf Slayer 7)
		String craftText = recipe.getExtraText().getString();

		if (!craftText.isEmpty()) {
			if (textRenderer.width(craftText) > MAX_TEXT_WIDTH) {
				//Set craft text as hovered text if we're hovering over it since it got truncated
				if (isMouseHoveringText(x + 11, y + 31, mouseX, mouseY)) this.hoveredText = Component.literal(craftText);

				craftText = textRenderer.plainSubstrByWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS_STRING;
			}

			context.drawString(textRenderer, craftText, x + 11, y + 31, CommonColors.WHITE);
		}

		//Render the resulting item's name
		Component itemName = recipe.getOutputs().getFirst().getHoverName();

		if (textRenderer.width(itemName) > MAX_TEXT_WIDTH) {
			FormattedText trimmed = FormattedText.composite(textRenderer.substrByWidth(itemName, MAX_TEXT_WIDTH), CommonComponents.ELLIPSIS);
			FormattedCharSequence ordered = Language.getInstance().getVisualOrder(trimmed);

			context.drawString(textRenderer, ordered, x + 11, y + 43, CommonColors.WHITE);

			//Set the resulting item's name as hovered text if we're hovering over it since the text got truncated
			if (isMouseHoveringText(x + 11, y + 43, mouseX, mouseY)) this.hoveredText = itemName;
		} else {
			context.drawString(textRenderer, itemName, x + 11, y + 43, CommonColors.WHITE);
		}

		//Draw the arrow that points to the recipe's result
		context.drawString(textRenderer, "▶", x + 96, y + 90, 0xAAFFFFFF);
		if (this.hoveredText == null && mouseX >= x + 86 && mouseY >= y + 81 && mouseX < x + 86 + 25 && mouseY < y + 81 + 25 && recipe instanceof SkyblockForgeRecipe forgeRecipe) {
			this.hoveredText = Component.nullToEmpty(forgeRecipe.getDurationString());
		}
		if (recipeIcon != null) context.renderItem(recipeIcon, x + 115, y + 61);
	}

	@Override
	public void drawTooltip(GuiGraphics context, int x, int y) {
		if (this.client.screen != null) {
			//Draw the tooltip of the hovered result button if one is hovered over
			if (this.hoveredResultButton != null && !this.hoveredResultButton.getDisplayStack().isEmpty()) {
				ItemStack stack = this.hoveredResultButton.getDisplayStack();
				ResourceLocation tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);

				context.setComponentTooltipForNextFrame(this.client.font, SkyblockRecipeResultButton.getTooltip(stack), x, y, tooltipStyle);
			} else if (this.hoveredText != null) {
				//Draw text as a tooltip if it got truncated & we're hovering over it (for recipe display)
				context.setTooltipForNextFrame(this.client.font, this.hoveredText, x, y);
			}
		}
	}

	/**
	 * Returns true if the mouse is hovering over the text at this location.
	 */
	private boolean isMouseHoveringText(int textX, int textY, int mouseX, int mouseY) {
		return HudHelper.pointIsInArea(mouseX, mouseY, textX, textY, textX + MAX_TEXT_WIDTH + 4, textY + this.client.font.lineHeight);
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
				String name = stack.getHoverName().getString().toLowerCase(Locale.ENGLISH);
				if (!filterOption.test(name)) continue;

				List<String> lore = stack.skyblocker$getLoreStrings();

				if (name.contains(query) || lore.stream()
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
					int startY = button.getY() + button.getHeight() / 2 - (gridSize.y * 25) / 2;
					for (int i = 0; i < forgeRecipe.getInputs().size(); i++) {
						int x = startX + (i % gridSize.x) * 25;
						int y = startY + (i / gridSize.x) * 25;
						recipeSlotButtons.add(new SkyblockRecipeResultButton(x, y).setDisplayStack(forgeRecipe.getInputs().get(i)));
					}
					//Result
					recipeSlotButtons.add(this.resultButtons.get(14).setDisplayStack(forgeRecipe.getResult()));
				}
				case SkyblockNpcShopRecipe npcShopRecipe -> {
					recipeIcon = new ItemStack(Items.GOLD_NUGGET);

					recipeSlotButtons.add(this.resultButtons.get(8).setDisplayStack(npcShopRecipe.getNpcItem()));

					int slotsPerRow = 3;
					int rows = npcShopRecipe.getInputs().size() / slotsPerRow + 1;
					// Using this slot as a center cuz I said so again
					SkyblockRecipeResultButton button = this.resultButtons.get(11);
					int startX = this.resultButtons.getFirst().getX();
					int startY = button.getY() + button.getHeight() / 2 - (rows * 25) / 2;
					for (int i = 0; i < npcShopRecipe.getInputs().size(); i++) {
						int x = startX + (i % slotsPerRow) * 25;
						int y = startY + (i / slotsPerRow) * 25;
						recipeSlotButtons.add(new SkyblockRecipeResultButton(x, y).setDisplayStack(npcShopRecipe.getInputs().get(i)));
					}

					recipeSlotButtons.add(this.resultButtons.get(14).setDisplayStack(npcShopRecipe.getOutputs().getFirst()));

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
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (this.nextPageButton.mouseClicked(click, doubled)) {
			this.currentPage++;
			this.updateResultButtons();

			return true;
		} else if (this.prevPageButton.mouseClicked(click, doubled)) {
			this.currentPage--;
			this.updateResultButtons();

			return true;
		}

		if (this.recipeView && click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			// The crafting result button
			var result = resultButtons.get(14);
			var rawID = result.getDisplayStack().getSkyblockId();
			if (result.isMouseOver(click.x(), click.y())) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown(String.format("/viewrecipe %s", rawID), true);
				return true;
			}
		}

		for (SkyblockRecipeResultButton resultButton : recipeView ? recipeSlotButtons : this.resultButtons) {
			//If the result button was clicked then try and show a recipe if there is one
			//for the item
			if (resultButton.mouseClicked(click, doubled)) {
				String itemId = resultButton.getDisplayStack().getSkyblockId();

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

	private @Nullable ItemStack getHoveredItemStack(double mouseX, double mouseY) {
		return resultButtons.stream().filter(btn -> btn.isMouseOver(mouseX, mouseY)).findFirst().map(SkyblockRecipeResultButton::getDisplayStack).orElse(null);
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, KeyEvent input) {
		ItemStack hovered = getHoveredItemStack(mouseX, mouseY);
		if (hovered == null) return false;

		if (WikiLookupManager.handleWikiLookup(Either.right(hovered), client.player, input)) {
			return true;
		}

		if (SkyblockerConfigManager.get().helpers.itemPrice.enableItemPriceLookup && ItemPrice.ITEM_PRICE_LOOKUP.matches(input)) {
			ItemPrice.itemPriceLookup(client.player, hovered);
			return true;
		}
		return false;
	}
}
