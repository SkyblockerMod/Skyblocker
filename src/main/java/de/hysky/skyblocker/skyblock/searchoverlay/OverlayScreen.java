package de.hysky.skyblocker.skyblock.searchoverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import static de.hysky.skyblocker.skyblock.itemlist.ItemRepository.getItemStack;

public class OverlayScreen extends Screen {

	protected static final Identifier SEARCH_ICON_TEXTURE = Identifier.withDefaultNamespace("icon/search");
	protected static final Identifier DELETE_ICON_TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/pending_invite/reject.png");
	private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("social_interactions/background");
	private static final int rowHeight = 20;
	private static final int specialButtonSize = rowHeight;
	private EditBox searchField;
	private Button finishedButton;
	private Button maxPetButton;
	private Button dungeonStarButton;
	private Button[] suggestionButtons;
	private Button[] historyButtons;
	private Button[] deleteButtons;

	public OverlayScreen() {
		super(Component.empty());
	}

	/**
	 * Creates the layout for the overlay screen.
	 */
	@Override
	protected void init() {
		super.init();
		int rowWidth = (int) (this.width * 0.4);
		int startX = (int) (this.width * 0.5) - rowWidth / 2;
		int startY = (int) ((int) (this.height * 0.5) - (rowHeight * (1 + SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions + 0.75 + SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength)) / 2);

		// Search field
		this.searchField = new EditBox(font, startX, startY, rowWidth - rowHeight, rowHeight, Component.translatable("gui.recipebook.search_hint"));
		searchField.setValue(SearchOverManager.search);
		searchField.setResponder(SearchOverManager::updateSearch);
		searchField.setMaxLength(30);

		// finish buttons
		finishedButton = Button.builder(Component.literal(""), a -> onClose())
				.pos(startX + rowWidth - rowHeight, startY)
				.size(specialButtonSize, specialButtonSize).build();

		// suggested item buttons
		int totalSuggestions = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions;
		this.suggestionButtons = new Button[totalSuggestions];
		LinearLayout suggestionLayoutWidget = new LinearLayout(startX, startY + rowHeight, LinearLayout.Orientation.VERTICAL);
		for (int i = 0; i < totalSuggestions; i++) {
			suggestionButtons[i] = Button.builder(Component.empty(), a -> {
				SearchOverManager.updateSearch(a.getMessage().getString());
				onClose();
			}).size(rowWidth, rowHeight).build();
			suggestionLayoutWidget.addChild(suggestionButtons[i]);
			suggestionButtons[i].visible = false;
		}

		// history item buttons
		int historyOffset = (int) (rowHeight * (totalSuggestions + 1.75));
		int historyLength = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength;
		this.historyButtons = new Button[historyLength];
		this.deleteButtons = new Button[historyLength];
		GridLayout historyGridWidget = new GridLayout(startX, startY + historyOffset);
		GridLayout.RowHelper historyAdder = historyGridWidget.createRowHelper(2);
		for (int i = 0; i < historyLength; i++) {
			historyButtons[i] = Button.builder(Component.empty(), (a) -> {
				SearchOverManager.search = a.getMessage().getString();
				SearchOverManager.updateSearch(a.getMessage().getString());
				onClose();
			}).size(rowWidth - rowHeight, rowHeight).build();
			historyButtons[i].visible = false;
			historyAdder.addChild(historyButtons[i]);

			final int slotId = i;
			deleteButtons[i] = Button.builder(Component.empty(), (a) -> removeHistoryItem(slotId)).size(specialButtonSize, specialButtonSize)
					.tooltip(Tooltip.create(Component.translatable("skyblocker.config.general.searchOverlay.deleteTooltip"))).build();
			deleteButtons[i].visible = false;
			historyAdder.addChild(deleteButtons[i]);
		}
		updateHistoryButtons();

		//auction only elements
		if (SearchOverManager.location == SearchOverManager.SearchLocation.AUCTION) {
			//max pet level button
			maxPetButton = Button.builder(Component.literal(""), a -> {
						SearchOverManager.maxPetLevel = !SearchOverManager.maxPetLevel;
						updateMaxPetText();
					})
					.tooltip(Tooltip.create(Component.translatable("skyblocker.config.general.searchOverlay.maxPet.@Tooltip")))
					.pos(startX, startY - rowHeight - 8)
					.size(rowWidth / 2, rowHeight).build();
			updateMaxPetText();

			//dungeon star input
			dungeonStarButton = Button.builder(Component.literal("✪"), a -> updateStars())
					.tooltip(Tooltip.create(Component.translatable("skyblocker.config.general.searchOverlay.starsTooltip")))
					.pos(startX + (int) (rowWidth * 0.5), startY - rowHeight - 8)
					.size(rowWidth / 2, rowHeight).build();

			updateStars();
		}

		suggestionLayoutWidget.arrangeElements();
		historyGridWidget.arrangeElements();

		//add drawables in order to make tab navigation sensible
		addRenderableWidget(searchField);
		suggestionLayoutWidget.visitWidgets(this::addRenderableWidget);
		historyGridWidget.visitWidgets(this::addRenderableWidget);
		addRenderableWidget(finishedButton);

		if (SearchOverManager.location == SearchOverManager.SearchLocation.AUCTION) {
			addRenderableWidget(maxPetButton);
			addRenderableWidget(dungeonStarButton);
		}

		//focus the search box
		this.setInitialFocus(searchField);
	}

	/**
	 * Finds if the mouse is clicked on the dungeon star button and if so works out what stars the user clicked on
	 * @return super
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (dungeonStarButton != null && dungeonStarButton.isHovered()) {
			double actualTextWidth = minecraft.font.width(dungeonStarButton.getMessage());
			double textOffset = (dungeonStarButton.getWidth() - actualTextWidth) / 2;
			double offset = click.x() - (dungeonStarButton.getX() + textOffset);
			int starCount = (int) ((offset / actualTextWidth) * 10);
			starCount = Math.clamp(starCount + 1, 0, 10);
			//if same as old value set stars to 0 else set to selected amount
			if (starCount == SearchOverManager.dungeonStars) {
				SearchOverManager.dungeonStars = 0;
			} else {
				SearchOverManager.dungeonStars = starCount;
			}
		}

		return super.mouseClicked(click, doubled);
	}

	/**
	 * Updates the text displayed on the max pet level button to represent the settings current state
	 */
	private void updateMaxPetText() {
		if (SearchOverManager.maxPetLevel) {
			maxPetButton.setMessage(Component.translatable("skyblocker.config.general.searchOverlay.maxPet").append(Component.literal(" ✔")).withStyle(ChatFormatting.GREEN));
		} else {
			maxPetButton.setMessage(Component.translatable("skyblocker.config.general.searchOverlay.maxPet").append(Component.literal(" ❌")).withStyle(ChatFormatting.RED));
		}
	}

	/**
	 * Updates stars in dungeon star input to represent the current star value
	 */
	private void updateStars() {
		MutableComponent stars = Component.empty();
		for (int i = 0; i < SearchOverManager.dungeonStars; i++) {
			stars.append(Component.literal("✪").withStyle(i < 5 ? ChatFormatting.YELLOW : ChatFormatting.RED));
		}
		for (int i = SearchOverManager.dungeonStars; i < 10; i++) {
			stars.append(Component.literal("✪"));
		}
		dungeonStarButton.setMessage(stars);
	}

	private void removeHistoryItem(int slotId) {
		SearchOverManager.removeHistoryItem(slotId);
		updateHistoryButtons();
	}

	private void updateHistoryButtons() {
		int historyLength = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength;
		for (int i = 0; i < historyLength; i++) {
			String text = SearchOverManager.getHistory(i);
			if (text.isEmpty()) {
				historyButtons[i].visible = false;
				deleteButtons[i].visible = false;
				continue;
			}

			historyButtons[i].setMessage(Component.literal(text));
			historyButtons[i].visible = true;
			deleteButtons[i].visible = true;
		}
	}

	/**
	 * Renders the background for the search using the social interactions background
	 * @param context context
	 * @param mouseX mouseX
	 * @param mouseY mouseY
	 * @param delta delta
	 */
	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		//find max height
		int maxHeight = rowHeight * (1 + suggestionButtons.length + historyButtons.length);
		if (historyButtons.length > 0) { //add space for history label if it could exist
			maxHeight += (int) (rowHeight * 0.75);
		}
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, searchField.getX() - 8, searchField.getY() - 8, (int) (this.width * 0.4) + 16, maxHeight + 16);
	}

	/**
	 * Renders the search icon, label for the history and item Stacks for item names
	 */
	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int renderOffset = (rowHeight - 16) / 2;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, SEARCH_ICON_TEXTURE, finishedButton.getX() + renderOffset, finishedButton.getY() + renderOffset, 16, 16);
		if (historyButtons.length > 0 && historyButtons[0] != null) {
			context.drawString(font, Component.translatable("skyblocker.config.general.searchOverlay.historyLabel"), historyButtons[0].getX() + renderOffset, historyButtons[0].getY() - rowHeight / 2, 0xFFFFFFFF, true);
		}

		//draw item stacks and tooltip to buttons
		for (int i = 0; i < suggestionButtons.length; i++) {
			drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getSuggestionId(i), suggestionButtons[i], renderOffset);
		}
		for (int i = 0; i < historyButtons.length; i++) {
			drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getHistoryId(i), historyButtons[i], renderOffset);
		}

		for (Button deleteButton : deleteButtons) {
			if (!deleteButton.visible) break;
			context.blit(RenderPipelines.GUI_TEXTURED, DELETE_ICON_TEXTURE, deleteButton.getX() + renderOffset, deleteButton.getY() + renderOffset, 0, 0, 16, 16, 16, 16);
		}
	}

	/**
	 * Draws the item and tooltip for the given button
	 */
	private void drawItemAndTooltip(GuiGraphics context, int mouseX, int mouseY, String id, Button button, int renderOffset) {
		if (id.isEmpty()) return;
		ItemStack item = getItemStack(id);
		if (item == null) return;
		context.renderItem(item, button.getX() + renderOffset, button.getY() + renderOffset);

		// Draw tooltip
		if (button.isMouseOver(mouseX, mouseY)) {
			context.setTooltipForNextFrame(font, item, mouseX, mouseY);
		}
	}

	/**
	 * updates if the suggestions buttons should be visible based on if they have a value
	 */
	@Override
	public final void tick() {
		super.tick();
		//update suggestion buttons text
		for (int i = 0; i < SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions; i++) {
			String text = SearchOverManager.getSuggestion(i);
			if (!text.isEmpty()) {
				suggestionButtons[i].visible = true;

				boolean isNewText = !text.equals(suggestionButtons[i].getMessage().getString());
				if (!isNewText) continue;

				suggestionButtons[i].setMessage(Component.literal(text));
			} else {
				suggestionButtons[i].visible = false;
			}
		}
	}

	/**
	 * When a key is pressed. If enter key pressed and search box selected close
	 */
	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.isConfirmation() && searchField.canConsumeInput()) {
			onClose();
			return true;
		}
		if (input.isEscape()) {
			SearchOverManager.search = "";
			onClose();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/**
	 * Closes the overlay screen and gets the manager to send a packet update about the sign
	 */
	@Override
	public void onClose() {
		assert this.minecraft.player != null;
		SearchOverManager.pushSearch();
		super.onClose();
	}
}
