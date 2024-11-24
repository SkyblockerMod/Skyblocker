package de.hysky.skyblocker.skyblock.museum;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.skyblock.item.MuseumItemCache;
import de.hysky.skyblocker.skyblock.item.WikiLookup;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MuseumManager extends ClickableWidget {
	private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/recipe_book.png");
	private static final int BACKGROUND_WIDTH = 147;
	private static final int BACKGROUND_HEIGHT = 166;

	private static final int SEARCH_FIELD_WIDTH = 69;
	private static final int SEARCH_FIELD_HEIGHT = 20;
	private static final int BUTTON_SIZE = 20;
	private static final int BUTTONS_PER_PAGE = 12;
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final ItemSorter ITEM_SORTER = new ItemSorter();
	private static final ItemFilter ITEM_FILTER = new ItemFilter();
	private static final TextRenderer TEXT_RENDERER = CLIENT.textRenderer;
	private static String SEARCH_QUERY = "";
	private static int CURRENT_PAGE = 0;
	private final int layoutX;
	private final int layoutY;
	private final ToggleButtonWidget nextPageButton;
	private final ToggleButtonWidget prevPageButton;
	private final TextFieldWidget searchField;
	private final List<Donation> allDonations;
	private final List<Donation> filteredDonations = new ArrayList<>();
	private final List<String> excludedDonationIds = new ArrayList<>();
	private final List<DonationButton> donationButtons = Lists.newArrayListWithCapacity(BUTTONS_PER_PAGE);
	private final ButtonWidget filterButton;
	private final ButtonWidget sortButton;
	private DonationButton hoveredDonationButton;
	private int pageCount = 0;

	public MuseumManager(Screen screen, int x, int y, int backgroundWidth) {
		super(x, y, screen.width, screen.height, Text.of(""));
		this.layoutX = getX() + backgroundWidth + 2;
		this.layoutY = y;

		// Initialize search field
		this.searchField = new TextFieldWidget(TEXT_RENDERER, layoutX + 25, layoutY + 11, SEARCH_FIELD_WIDTH, SEARCH_FIELD_HEIGHT, Text.of("Search"));
		this.searchField.setMaxLength(60);
		this.searchField.setVisible(true);
		this.searchField.setEditableColor(0xFFFFFF);
		this.searchField.setText(SEARCH_QUERY);
		this.searchField.setPlaceholder(Text.literal("Search...").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));

		// Initialize page navigation buttons
		this.nextPageButton = new ToggleButtonWidget(layoutX + 93, layoutY + 133, 12, 17, false);
		this.nextPageButton.setTextures(RecipeBookResults.PAGE_FORWARD_TEXTURES);
		this.prevPageButton = new ToggleButtonWidget(layoutX + 38, layoutY + 133, 12, 17, true);
		this.prevPageButton.setTextures(RecipeBookResults.PAGE_BACKWARD_TEXTURES);

		this.allDonations = MuseumItemCache.getDonations();

		// Create donation buttons for pagination
		for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
			DonationButton button = new DonationButton(layoutX + 11 + 31 * (i % 4), layoutY + 34 + 31 * (i / 4));
			this.donationButtons.add(button);
		}

		// Initialize sort button
		this.sortButton = ButtonWidget.builder(Text.literal(""), button -> {
					ITEM_SORTER.cycleSortMode(filteredDonations);
					button.setTooltip(ITEM_SORTER.getTooltip());
					CURRENT_PAGE = 0;
					updateButtons();
				})
				.tooltip(ITEM_SORTER.getTooltip())
				.position(layoutX + 95, layoutY + 11)
				.size(BUTTON_SIZE, BUTTON_SIZE)
				.build();

		// Initialize filter button
		this.filterButton = ButtonWidget.builder(Text.literal(""), button -> {
					ITEM_FILTER.cycleFilterMode(allDonations, filteredDonations);
					ITEM_SORTER.applySort(filteredDonations);
					button.setTooltip(ITEM_FILTER.getTooltip());
					CURRENT_PAGE = 0;
					updateButtons();
				})
				.tooltip(ITEM_FILTER.getTooltip())
				.position(layoutX + 116, layoutY + 11)
				.size(BUTTON_SIZE, BUTTON_SIZE)
				.build();

		ITEM_FILTER.applyFilter(allDonations, filteredDonations);
		ITEM_SORTER.applySort(filteredDonations);
		updateSearchResults(false);

		((ScreenAccessor) screen).callAddDrawableChild(this);
		screen.setFocused(this);
	}

	/**
	 * Resets the UI state including search text, current page, sorting, and filtering.
	 */
	public static void reset() {
		SEARCH_QUERY = "";
		CURRENT_PAGE = 0;
		ITEM_SORTER.resetSorting();
		ITEM_FILTER.resetFilter();
	}

	/**
	 * Updates visibility and content of page navigation buttons.
	 */
	private void updateNavigationButtons() {
		this.prevPageButton.active = CURRENT_PAGE > 0;
		this.nextPageButton.active = CURRENT_PAGE < pageCount - 1;
	}

	/**
	 * Updates the donation buttons based on the current page and visible donations.
	 */
	private void updateButtons() {
		List<Donation> visibleDonations = filteredDonations.stream()
				.filter(donation -> !excludedDonationIds.contains(donation.getId()))
				.toList();

		int buttonsSize = visibleDonations.size();
		this.pageCount = (int) Math.ceil((double) buttonsSize / BUTTONS_PER_PAGE);

		for (int i = 0; i < donationButtons.size(); ++i) {
			int index = CURRENT_PAGE * donationButtons.size() + i;

			if (index < buttonsSize) {
				donationButtons.get(i).init(visibleDonations.get(index));
			} else {
				donationButtons.get(i).clearDisplayStack();
			}
		}
		updateNavigationButtons();
	}

	/**
	 * Updates search results based on the search text.
	 *
	 * @param resetPage Whether to reset to the first page.
	 */
	public void updateSearchResults(boolean resetPage) {
		excludedDonationIds.clear();
		for (Donation item : allDonations) {
			StringBuilder searchableContent = new StringBuilder();
			ItemStack itemStack = ItemRepository.getItemStack(item.getId());
			if (itemStack != null) {
				searchableContent.append(itemStack.getName().getString())
						.append(ItemUtils.getConcatenatedLore(itemStack));
			}
			if (item.getSet() != null && !item.getSet().isEmpty()) {
				for (ArmorPiece piece : item.getSet()) {
					ItemStack pieceStack = ItemRepository.getItemStack(piece.getId());
					if (pieceStack != null) searchableContent.append(pieceStack.getName().getString())
							.append(ItemUtils.getConcatenatedLore(pieceStack));
				}
			}
			if (!searchableContent.toString().toLowerCase().contains(SEARCH_QUERY.toLowerCase())) {
				excludedDonationIds.add(item.getId());
			}
		}
		if (resetPage) CURRENT_PAGE = 0;
		updateButtons();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render the background texture for the widget
		context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, layoutX, layoutY, 1.0f, 1.0f, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256, 256 - 10);

		// Render page count if multiple pages exist
		if (this.pageCount > 1) {
			Text text = Text.translatable("gui.recipebook.page", CURRENT_PAGE + 1, this.pageCount);
			int width = TEXT_RENDERER.getWidth(text);

			context.drawText(TEXT_RENDERER, text, layoutX - width / 2 + 73, layoutY + 137, -1, false);
		}

		// Render donation buttons
		this.hoveredDonationButton = null;
		for (DonationButton resultButton : donationButtons) {
			resultButton.render(context, mouseX, mouseY, delta);

			if (resultButton.visible && resultButton.isHovered()) this.hoveredDonationButton = resultButton;
		}

		if (this.sortButton.active) {
			int iconX = this.sortButton.getX() + (this.sortButton.getWidth() - 16) / 2;
			int iconY = this.sortButton.getY() + (this.sortButton.getHeight() - 16) / 2;
			ItemStack stack = ITEM_SORTER.getCurrentSortingItem();
			context.drawItemWithoutEntity(stack, iconX, iconY);
		}

		if (this.filterButton.active) {
			int iconX = this.filterButton.getX() + (this.filterButton.getWidth() - 16) / 2;
			int iconY = this.filterButton.getY() + (this.filterButton.getHeight() - 16) / 2;
			ItemStack stack = ITEM_FILTER.getCurrentFilterItem();
			context.drawItemWithoutEntity(stack, iconX, iconY);
		}

		// Render the page flip buttons
		if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
		if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);

		this.filterButton.render(context, mouseX, mouseY, delta);
		this.sortButton.render(context, mouseX, mouseY, delta);
		this.searchField.render(context, mouseX, mouseY, delta);

		this.drawTooltip(context, mouseX, mouseY);
	}

	public void drawTooltip(DrawContext context, int x, int y) {
		// Draw the tooltip of the hovered result button if one is hovered over
		if (this.hoveredDonationButton != null && !this.hoveredDonationButton.getDisplayStack().isEmpty()) {
			ItemStack stack = this.hoveredDonationButton.getDisplayStack();
			Identifier tooltipStyle = stack.get(DataComponentTypes.TOOLTIP_STYLE);

			context.drawTooltip(TEXT_RENDERER, hoveredDonationButton.getItemTooltip(), x, y, tooltipStyle);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
			this.searchField.setFocused(true);
			return true;
		} else if (this.nextPageButton.mouseClicked(mouseX, mouseY, button)) {
			CURRENT_PAGE++;
			updateButtons();
			return true;
		} else if (this.prevPageButton.mouseClicked(mouseX, mouseY, button)) {
			CURRENT_PAGE--;
			updateButtons();
			return true;
		} else if (this.filterButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		} else if (this.sortButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		this.searchField.setFocused(false);
		this.filterButton.setFocused(false);
		this.sortButton.setFocused(false);
		return false;
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (this.searchField.charTyped(chr, modifiers)) {
			SEARCH_QUERY = this.searchField.getText();
			this.updateSearchResults(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ((this.searchField.isActive() && keyCode == GLFW.GLFW_KEY_E)
				|| this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
			SEARCH_QUERY = this.searchField.getText();
			this.updateSearchResults(true);
			return true;
		} else if (WikiLookup.wikiLookup.matchesKey(keyCode, scanCode) && CLIENT.player != null && hoveredDonationButton != null && hoveredDonationButton.getDisplayStack() != null) {
			WikiLookup.openWiki(hoveredDonationButton.getDisplayStack(), CLIENT.player);
			return true;
		}
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
