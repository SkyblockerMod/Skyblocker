package de.hysky.skyblocker.skyblock.museum;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.item.ItemPrice;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MuseumManager extends ClickableWidget implements HoveredItemStackProvider {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final TextRenderer TEXT_RENDERER = CLIENT.textRenderer;
	private static final Identifier BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/recipe_book.png");
	public static final int BACKGROUND_WIDTH = 147;
	public static final int BACKGROUND_HEIGHT = 160;
	public static final int SPACING = 2;
	private static final int BUTTON_SIZE = 20;
	private static final int BUTTONS_PER_PAGE = 12;
	private static final ItemSorter ITEM_SORTER = new ItemSorter();
	private static final ItemFilter ITEM_FILTER = new ItemFilter();
	private static String searchQuery = "";
	private static int currentPage = 0;
	private static List<Donation> donations = new ArrayList<>();
	private final ToggleButtonWidget nextPageButton;
	private final ToggleButtonWidget prevPageButton;
	private final TextFieldWidget searchField;
	private final List<Donation> filteredDonations = new ArrayList<>();
	private final List<String> excludedDonationIds = new ArrayList<>();
	private final List<DonationButton> donationButtons = Lists.newArrayListWithCapacity(BUTTONS_PER_PAGE);
	private final ButtonWidget filterButton;
	private final ButtonWidget sortButton;
	private DonationButton hoveredDonationButton;
	private int pageCount = 0;

	public MuseumManager(Screen screen, int x, int y, int backgroundWidth) {
		super(x + backgroundWidth + SPACING, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, Text.empty());

		// Initialize search field
		this.searchField = new TextFieldWidget(TEXT_RENDERER, getX() + 25, getY() + 11, 69, 20, Text.empty());
		this.searchField.setMaxLength(60);
		this.searchField.setVisible(true);
		this.searchField.setEditableColor(Colors.WHITE);
		this.searchField.setText(searchQuery);
		this.searchField.setPlaceholder(Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));

		// Initialize page navigation buttons
		this.nextPageButton = new ToggleButtonWidget(getX() + 93, getY() + 133, 12, 17, false);
		this.nextPageButton.setTextures(RecipeBookResults.PAGE_FORWARD_TEXTURES);
		this.prevPageButton = new ToggleButtonWidget(getX() + 38, getY() + 133, 12, 17, true);
		this.prevPageButton.setTextures(RecipeBookResults.PAGE_BACKWARD_TEXTURES);

		donations = MuseumItemCache.getDonations();

		// Create donation buttons for pagination
		for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
			DonationButton button = new DonationButton(getX() + 11 + 31 * (i % 4), getY() + 34 + 31 * (i / 4));
			this.donationButtons.add(button);
		}

		// Initialize sort button
		this.sortButton = ButtonWidget.builder(Text.empty(), button -> {
					ITEM_SORTER.cycleSortMode(filteredDonations);
					button.setTooltip(ITEM_SORTER.getTooltip());
					currentPage = 0;
					updateButtons();
				})
				.tooltip(ITEM_SORTER.getTooltip())
				.position(getX() + 95, getY() + 11)
				.size(BUTTON_SIZE, BUTTON_SIZE)
				.build();

		// Initialize filter button
		this.filterButton = ButtonWidget.builder(Text.empty(), button -> {
					ITEM_FILTER.cycleFilterMode(donations, filteredDonations);
					ITEM_SORTER.applySort(filteredDonations);
					button.setTooltip(ITEM_FILTER.getTooltip());
					currentPage = 0;
					updateButtons();
				})
				.tooltip(ITEM_FILTER.getTooltip())
				.position(getX() + 116, getY() + 11)
				.size(BUTTON_SIZE, BUTTON_SIZE)
				.build();

		ITEM_FILTER.applyFilter(donations, filteredDonations);
		ITEM_SORTER.applySort(filteredDonations);
		updateSearchResults(false);

		Screens.getButtons(screen).add(this);
		screen.setFocused(this);
	}

	/**
	 * Retrieves the Donation object corresponding to a given ID.
	 *
	 * @param id the ID of the donation to retrieve
	 * @return the Donation object associated with the given ID, or null if not found
	 */
	protected static Donation getDonation(String id) {
		return donations.stream()
				.filter(donation -> donation.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Updates visibility and content of page navigation buttons.
	 */
	private void updateNavigationButtons() {
		this.prevPageButton.active = currentPage > 0;
		this.nextPageButton.active = currentPage < pageCount - 1;
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
			int index = currentPage * donationButtons.size() + i;

			if (index < buttonsSize) {
				donationButtons.get(i).init(visibleDonations.get(index));
			} else {
				donationButtons.get(i).resetButton();
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
		searchQuery = this.searchField.getText();
		excludedDonationIds.clear();
		for (Donation item : donations) {
			StringBuilder searchableContent = new StringBuilder();
			ItemStack itemStack = ItemRepository.getItemStack(item.getId());
			if (itemStack != null) {
				searchableContent.append(itemStack.getName().getString())
						.append(ItemUtils.getConcatenatedLore(itemStack));
			}
			if (item.getSet() != null && !item.getSet().isEmpty()) {
				for (ObjectObjectMutablePair<String, PriceData> piece : item.getSet()) {
					ItemStack pieceStack = ItemRepository.getItemStack(piece.left());
					if (pieceStack != null) searchableContent.append(pieceStack.getName().getString())
							.append(ItemUtils.getConcatenatedLore(pieceStack));
				}
			}
			if (!searchableContent.toString().toLowerCase(Locale.ENGLISH).contains(searchQuery.toLowerCase(Locale.ENGLISH))) {
				excludedDonationIds.add(item.getId());
			}
		}
		if (resetPage) currentPage = 0;
		updateButtons();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render the background texture for the widget
		context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, getX(), getY(), 1.0f, 1.0f, getWidth(), getHeight(), 256, 256 - 10);
		searchField.render(context, mouseX, mouseY, delta);

		if (this.sortButton.active) {
			int iconX = this.sortButton.getX() + (this.sortButton.getWidth() - 16) / 2;
			int iconY = this.sortButton.getY() + (this.sortButton.getHeight() - 16) / 2;
			ItemStack stack = ITEM_SORTER.getCurrentSortingItem();
			sortButton.render(context, mouseX, mouseY, delta);
			context.drawItemWithoutEntity(stack, iconX, iconY);
		}

		if (this.filterButton.active) {
			int iconX = this.filterButton.getX() + (this.filterButton.getWidth() - 16) / 2;
			int iconY = this.filterButton.getY() + (this.filterButton.getHeight() - 16) / 2;
			ItemStack stack = ITEM_FILTER.getCurrentFilterItem();
			filterButton.render(context, mouseX, mouseY, delta);
			context.drawItemWithoutEntity(stack, iconX, iconY);
		}

		if (ItemRepository.filesImported()) {
			// Render page count if multiple pages exist
			if (this.pageCount > 1) {
				Text text = Text.translatable("gui.recipebook.page", currentPage + 1, this.pageCount);
				int width = TEXT_RENDERER.getWidth(text);

				context.drawText(TEXT_RENDERER, text, getX() - width / 2 + 73, getY() + 137, -1, false);
			}

			// Render donation buttons
			this.hoveredDonationButton = null;
			for (DonationButton resultButton : donationButtons) {
				resultButton.render(context, mouseX, mouseY, delta);

				if (resultButton.visible && resultButton.isHovered()) this.hoveredDonationButton = resultButton;
			}

			// Render the page flip buttons
			if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
			if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);

			drawTooltip(context, mouseX, mouseY);
		} else {
			context.drawCenteredTextWithShadow(TEXT_RENDERER, "Loading...", getX() + (BACKGROUND_WIDTH / 2), getY() + (BACKGROUND_HEIGHT / 2), Colors.WHITE);
		}
	}

	public void drawTooltip(DrawContext context, int x, int y) {
		// Draw the tooltip of the hovered result button if one is hovered over
		if (this.hoveredDonationButton != null) {
			context.drawTooltip(TEXT_RENDERER, hoveredDonationButton.getItemTooltip(), x, y, null);
		}
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (this.searchField.mouseClicked(click, doubled)) {
			this.searchField.setFocused(true);
			return true;
		} else if (this.nextPageButton.mouseClicked(click, doubled)) {
			currentPage++;
			updateButtons();
			return true;
		} else if (this.prevPageButton.mouseClicked(click, doubled)) {
			currentPage--;
			updateButtons();
			return true;
		} else if (this.filterButton.mouseClicked(click, doubled)) {
			return true;
		} else if (this.sortButton.mouseClicked(click, doubled)) {
			return true;
		}

		this.searchField.setFocused(false);

		return false;
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (this.searchField.charTyped(input)) {
			updateSearchResults(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if ((this.searchField.isActive() && CLIENT.options.inventoryKey.matchesKey(input))
				|| this.searchField.keyPressed(input)) {
			updateSearchResults(true);
			return true;
		}

		if (hoveredDonationButton != null) {
			ItemStack hoveredStack = hoveredDonationButton.getDisplayStack();
			if (hoveredStack == null) return false;
			if (WikiLookupManager.handleWikiLookup(Either.right(hoveredStack), CLIENT.player, input)) return true;
			if (ItemPrice.ITEM_PRICE_LOOKUP.matchesKey(input)) {
				ItemPrice.itemPriceLookup(CLIENT.player, hoveredStack);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public @Nullable ItemStack getFocusedItem() {
		if (hoveredDonationButton == null) return null;
		return hoveredDonationButton.getDisplayStack();
	}
}
