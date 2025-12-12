package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.mixins.accessors.RecipeBookWidgetAccessor;
import de.hysky.skyblocker.utils.render.gui.CyclingTextureWidget;
import it.unimi.dsi.fastutil.Pair;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Based on {@link net.minecraft.client.gui.screens.recipebook.RecipeBookComponent}.
 */
public class SkyblockRecipeBookWidget extends RecipeBookComponent<NoopRecipeScreenHandler> {
	protected static final int IMAGE_WIDTH = RecipeBookComponent.IMAGE_WIDTH;
	private static final int IMAGE_HEIGHT = RecipeBookComponent.IMAGE_HEIGHT;
	//Corresponds to field_32410 in RecipeBookWidget
	private static final int OFFSET_X_POSITION = 86;
	// 81 is the search field's width, 4 is the space between it and the toggle crafting button, and 26 is the toggle crafting button's width, which we replace
	// with the filtering button. 26 - 14 - 4 = 12 - 4 = 8 (The additional space left to the search field.)
	private static final int SEARCH_FIELD_WIDTH = 81 + 4 + 8;
	private static String lastSearch = "";
	/**
	 * The tabs in the Skyblock recipe book.
	 */
	private final List<RecipeTab> tabs = List.of(
			new SkyblockCraftingTab(this, SkyblockCraftingTab.CRAFTING_TABLE, new SkyblockRecipeResults()),
			new UpcomingEventsTab()
	);
	private final List<Pair<RecipeTab, SkyblockRecipeTabButton>> tabButtons = Lists.newArrayList();
	private Pair<RecipeTab, SkyblockRecipeTabButton> currentTab;

	protected CyclingTextureWidget<FilterOption> filterOption;

	public SkyblockRecipeBookWidget(AbstractContainerMenu screenHandler) {
		super(new NoopRecipeScreenHandler(screenHandler.containerId), List.of());
	}

	@Override
	public void init(int parentWidth, int parentHeight, Minecraft client, boolean narrow) {
		super.init(parentWidth, parentHeight, client, narrow);
	}

	@Override
	protected void initVisuals() {
		this.xOffset = this.widthTooNarrow ? 0 : OFFSET_X_POSITION;
		int left = accessor().invokeGetXOrigin();
		int top = accessor().invokeGetYOrigin();

		// Init Search Field only once
		if (this.searchBox == null) {
			this.searchBox = new EditBox(this.minecraft.font, left + 25, top + 13, SEARCH_FIELD_WIDTH, 14, Component.translatable("itemGroup.search"));
			this.searchBox.setMaxLength(60); //Set at 60 due to the longest Skyblock item name being 55 characters long
			this.searchBox.setTextColor(CommonColors.WHITE);
			this.searchBox.setValue(lastSearch);
			this.searchBox.setHint(SEARCH_HINT);
		}

		this.searchBox.setX(left + 25);
		this.searchBox.setY(top + 13);
		this.searchBox.setVisible(true);

		//This field's name is misleading, the rectangle is actually the area of the magnifying glass icon rather than the entire search field
		this.magnifierIconPlacement = ScreenRectangle.of(ScreenAxis.HORIZONTAL, left + 8, this.searchBox.getY(), this.searchBox.getX() - left, this.searchBox.getHeight());

		// Init filter option once
		if (this.filterOption == null) {
			this.filterOption = new CyclingTextureWidget<>(this.searchBox.getRight() + 4, this.searchBox.getY(), 14, 14, FilterOption.ALL);
			this.filterOption.setCycleListener(this::refilterSearchResults);
			this.filterOption.setTextSupplier(option -> Component.translatable("skyblocker.config.general.itemList.filter." + option.name().toLowerCase(Locale.ENGLISH)));
		}

		// Always update position of filter option
		this.filterOption.setX(this.searchBox.getRight() + 4);
		this.filterOption.setY(this.searchBox.getY());

		// Setup Tabs
		this.tabButtons.clear();

		for (RecipeTab tab : this.tabs) {
			this.tabButtons.add(Pair.of(tab, new SkyblockRecipeTabButton(tab.icon())));
		}

		// Since we clear the tabs when this is called, if a tab is set we need to update the instance
		if (this.currentTab != null) {
			this.currentTab = this.tabButtons.stream()
					.filter(button -> button.first().equals(this.currentTab.first()))
					.findFirst()
					.orElse(null);
		}

		// If there is no current tab, set it to the first one
		if (this.currentTab == null) {
			this.currentTab = this.tabButtons.getFirst();
		}

		this.currentTab.right().select();
		this.updateTabs(false);

		// Tab Init
		this.currentTab.left().initialize(this.minecraft, left, top);
		this.currentTab.left().updateSearchResults(this.searchBox.getValue().toLowerCase(Locale.ENGLISH), this.filterOption.getCurrent());
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (this.isVisible()) {
			int left = accessor().invokeGetXOrigin();
			int top = accessor().invokeGetYOrigin();
			context.blit(RenderPipelines.GUI_TEXTURED, RECIPE_BOOK_LOCATION, left, top, 1.0f, 1.0f, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

			for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
				tabButton.right().render(context, mouseX, mouseY, delta);
			}

			this.currentTab.left().draw(context, left, top, mouseX, mouseY, delta);
		}
	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, @Nullable Slot slot) {
		if (this.isVisible()) {
			this.currentTab.left().drawTooltip(context, x, y);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (this.isVisible() && !this.minecraft.player.isSpectator()) {
			if (this.currentTab.left().mouseClicked(click, doubled)) {
				return true;
			} else {
				for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
					if (tabButton.right().mouseClicked(click, doubled)) {
						if (this.currentTab != tabButton) {
							if (this.currentTab != null) this.currentTab.right().unselect();

							this.currentTab = tabButton;
							this.currentTab.right().select();
						}

						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		var client = Minecraft.getInstance();
		if (client.isWindowActive() && currentTab != null) {
			var mouse = client.mouseHandler;
			var window = client.getWindow();
			var mouseX = (mouse.xpos() * ((double) window.getGuiScaledWidth() / (double) window.getScreenWidth()));
			var mouseY = (mouse.ypos() * ((double) window.getGuiScaledHeight() / (double) window.getScreenHeight()));
			if (this.currentTab.left().keyPressed(mouseX, mouseY, input)) {
				return true;
			}
		}
		return super.keyPressed(input);
	}

	/**
	 * Same as the super classes implementation just that it checks for our custom tabs.
	 */
	@Override
	public boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int backgroundWidth, int backgroundHeight) {
		if (!this.isVisible()) {
			return true;
		} else {
			boolean bl = mouseX < (double) x || mouseY < (double) y || mouseX >= (double) (x + backgroundWidth) || mouseY >= (double) (y + backgroundHeight);
			boolean bl2 = (double) (x - 147) < mouseX && mouseX < (double) x && (double) y < mouseY && mouseY < (double) (y + backgroundHeight);

			return bl && !bl2 && !this.currentTab.right().isHoveredOrFocused();
		}
	}

	/**
	 * Refreshes the positions of our tabs.
	 */
	@Override
	protected void updateTabs(boolean filteringCraftable) {
		int i = accessor().invokeGetXOrigin() - 30;
		int j = accessor().invokeGetYOrigin() + 3;
		int l = 0;

		for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
			tabButton.right().setPosition(i, j + 27 * l++);
		}
	}

	protected void refilterSearchResults(FilterOption filterOption) {
		assert this.searchBox != null;
		lastSearch = this.searchBox.getValue();
		String query = this.searchBox.getValue().toLowerCase(Locale.ENGLISH);
		// Doesn't trigger the pirate speak check since the query wasn't changed.
		this.currentTab.left().updateSearchResults(query, filterOption, true);
	}

	@Override
	protected void checkSearchStringUpdate() {
		assert this.searchBox != null;
		lastSearch = this.searchBox.getValue();
		String query = this.searchBox.getValue().toLowerCase(Locale.ENGLISH);

		this.pirateSpeechForThePeople(query);
		//Note: The rest of the query checks are implemented by the results class
		this.currentTab.left().updateSearchResults(query, this.filterOption.getCurrent());
	}

	private RecipeBookWidgetAccessor accessor() {
		return (RecipeBookWidgetAccessor) this;
	}

	@Override
	protected void updateCollections(boolean resetCurrentPage, boolean filteringCraftable) {
		assert this.searchBox != null;
		this.currentTab.left().updateSearchResults(this.searchBox.getValue().toLowerCase(Locale.ENGLISH),
				this.filterOption.getCurrent());
	}

	/**
	 * Sets the "Toggle Craftable" Button texture.
	 *
	 * No-op as we don't use the button.
	 */
	@Override
	protected @Nullable WidgetSprites getFilterButtonTextures() {
		return null;
	}

	/**
	 * No-op.
	 */
	@Override
	protected boolean isCraftingSlot(Slot slot) {
		return false;
	}

	/**
	 * No-op.
	 */
	@Override
	protected void selectMatchingRecipes(RecipeCollection recipeResultCollection, StackedItemContents recipeFinder) {}

	/**
	 * No-op since we don't show the button.
	 */
	@Override
	protected Component getRecipeFilterName() {
		return null;
	}

	/**
	 * No-op.
	 */
	@Override
	protected void fillGhostRecipe(GhostSlots ghostRecipe, RecipeDisplay display, ContextMap context) {}

	/**
	 * No-op. Prevents a crash.
	 */
	@Override
	public void updateNarration(NarrationElementOutput builder) {}
}
