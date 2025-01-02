package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.List;
import java.util.Locale;

import de.hysky.skyblocker.utils.render.gui.CyclingTextureWidget;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import de.hysky.skyblocker.mixins.accessors.RecipeBookWidgetAccessor;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.context.ContextParameterMap;

/**
 * Based on {@link net.minecraft.client.gui.screen.recipebook.RecipeBookWidget}.
 */
public class SkyblockRecipeBookWidget extends RecipeBookWidget<NoopRecipeScreenHandler> {
	private static final int IMAGE_WIDTH = RecipeBookWidget.field_32408;
	private static final int IMAGE_HEIGHT = RecipeBookWidget.field_32409;
	//Corresponds to field_32410 in RecipeBookWidget
	private static final int OFFSET_X_POSITION = 86;
	// 81 is the search field's width, 4 is the space between it and the toggle crafting button, and 26 is the toggle crafting button's width, which we replace
	// with the filtering button. 26 - 14 - 4 = 12 - 4 = 8 (The additional space left to the search field.)
	private static final int SEARCH_FIELD_WIDTH = 81 + 4 + 8;
	/**
	 * The tabs in the Skyblock recipe book.
	 */
	private final List<RecipeTab> tabs = List.of(
			new SkyblockCraftingTab(this, SkyblockCraftingTab.CRAFTING_TABLE, new SkyblockCraftingRecipeResults()),
			new UpcomingEventsTab()
			);
	private final List<Pair<RecipeTab, SkyblockRecipeTabButton>> tabButtons = Lists.newArrayList();
	private Pair<RecipeTab, SkyblockRecipeTabButton> currentTab;

	protected CyclingTextureWidget<FilterOption> filterOption;

	public SkyblockRecipeBookWidget(ScreenHandler screenHandler) {
		super(new NoopRecipeScreenHandler(screenHandler.syncId), List.of());
	}

	@Override
	public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow) {
		super.initialize(parentWidth, parentHeight, client, narrow);
	}

	@Override
	protected void reset() {
		this.leftOffset = this.narrow ? 0 : OFFSET_X_POSITION;
		int left = accessor().invokeGetLeft();
		int top = accessor().invokeGetTop();

		//Init Search Field
		String defaultSearchText = this.searchField != null ? this.searchField.getText() : "";
		this.searchField = new TextFieldWidget(this.client.textRenderer, left + 25, top + 13, SEARCH_FIELD_WIDTH, 9 + 5, Text.translatable("itemGroup.search"));
		this.searchField.setMaxLength(60); //Set at 60 due to the longest Skyblock item name being 55 characters long
		this.searchField.setVisible(true);
		this.searchField.setEditableColor(0xFFFFFF);
		this.searchField.setText(defaultSearchText);
		this.searchField.setPlaceholder(SEARCH_HINT_TEXT);
		//This field's name is misleading, the rectangle is actually the area of the magnifying glass icon rather than the entire search field
		this.searchFieldRect = ScreenRect.of(NavigationAxis.HORIZONTAL, left + 8, this.searchField.getY(), this.searchField.getX() - left, this.searchField.getHeight());

		this.filterOption = new CyclingTextureWidget<>(this.searchField.getRight() + 4, this.searchField.getY(), 14, 14, FilterOption.ALL);
		this.filterOption.setCycleListener(this::refilterSearchResults);
		this.filterOption.setTextSupplier(option -> Text.translatable("skyblocker.config.general.itemList.filter." + option.name().toLowerCase(Locale.ENGLISH)));

		//Setup Tabs
		this.tabButtons.clear();

		for (RecipeTab tab : this.tabs) {
			this.tabButtons.add(Pair.of(tab, new SkyblockRecipeTabButton(tab.icon())));
		}

		//Since we clear the tabs when this is called, if a tab is set we need to update the instance
		if (this.currentTab != null) {
			this.currentTab = this.tabButtons.stream()
					.filter(button -> button.first().equals(this.currentTab.first()))
					.findFirst()
					.orElse(null);
		}

		//If there is no current tab, set it to the first one
		if (this.currentTab == null) {
			this.currentTab = this.tabButtons.getFirst();
		}

		//Set the current tab as toggled & refresh positions
		this.currentTab.right().setToggled(true);
		this.refreshTabButtons(false);

		//Tab Init
		this.currentTab.left().initialize(this.client, left, top);
		this.currentTab.left().initializeSearchResults(defaultSearchText);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (this.isOpen()) {
			context.getMatrices().push();
			context.getMatrices().translate(0.0f, 0.0f, 100.0f);
			int left = accessor().invokeGetLeft();
			int top = accessor().invokeGetTop();
			context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, left, top, 1.0f, 1.0f, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

			for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
				tabButton.right().render(context, mouseX, mouseY, delta);
			}

			this.currentTab.left().draw(context, left, top, mouseX, mouseY, delta);
			context.getMatrices().pop();
		}
	}

	@Override
	public void drawTooltip(DrawContext context, int x, int y, @Nullable Slot slot) {
		if (this.isOpen()) {
			this.currentTab.left().drawTooltip(context, x, y);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.isOpen() && !this.client.player.isSpectator()) {
			if (this.currentTab.left().mouseClicked(mouseX, mouseY, button)) {
				return true;
			} else {
				for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
					if (tabButton.right().mouseClicked(mouseX, mouseY, button)) {
						if (this.currentTab != tabButton) {
							if (this.currentTab != null) this.currentTab.right().setToggled(false);

							this.currentTab = tabButton;
							this.currentTab.right().setToggled(true);
						}

						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var client = MinecraftClient.getInstance();
		if (client.isWindowFocused()) {
			var mouse = client.mouse;
			var window = client.getWindow();
			var mouseX = (mouse.getX() * ((double) window.getScaledWidth() / (double) window.getWidth()));
			var mouseY = (mouse.getY() * ((double) window.getScaledHeight() / (double) window.getHeight()));
			if (this.currentTab.left().keyPressed(mouseX, mouseY, keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	/**
	 * Same as the super classes implementation just that it checks for our custom tabs.
	 */
	@Override
	public boolean isClickOutsideBounds(double mouseX, double mouseY, int x, int y, int backgroundWidth, int backgroundHeight, int button) {
		if (!this.isOpen()) {
			return true;
		} else {
			boolean bl = mouseX < (double) x || mouseY < (double) y || mouseX >= (double) (x + backgroundWidth) || mouseY >= (double) (y + backgroundHeight);
			boolean bl2 = (double) (x - 147) < mouseX && mouseX < (double) x && (double) y < mouseY && mouseY < (double) (y + backgroundHeight);

			return bl && !bl2 && !this.currentTab.right().isSelected();
		}
	}

	/**
	 * Refreshes the positions of our tabs.
	 */
	@Override
	protected void refreshTabButtons(boolean filteringCraftable) {
		int i = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
		int j = (this.parentHeight - 166) / 2 + 3;
		int l = 0;

		for (Pair<RecipeTab, SkyblockRecipeTabButton> tabButton : this.tabButtons) {
			tabButton.right().setPosition(i, j + 27 * l++);
		}
	}

	protected void refilterSearchResults(FilterOption filterOption) {
		assert this.searchField != null;
		String query = this.searchField.getText().toLowerCase(Locale.ENGLISH);
		// Doesn't trigger the pirate speak check since the query wasn't changed.
		this.currentTab.left().updateSearchResults(query, filterOption, true);
	}

	@Override
	protected void refreshSearchResults() {
		assert this.searchField != null;
		String query = this.searchField.getText().toLowerCase(Locale.ENGLISH);

		this.triggerPirateSpeakEasterEgg(query);
		//Note: The rest of the query checks are implemented by the results class
		this.currentTab.left().updateSearchResults(query, this.filterOption.getCurrent());
	}

	private RecipeBookWidgetAccessor accessor() {
		return (RecipeBookWidgetAccessor) this;
	}

	@Override
	protected void refreshResults(boolean resetCurrentPage, boolean filteringCraftable) {
		assert this.searchField != null;
		this.currentTab.left().updateSearchResults(this.searchField.getText().toLowerCase(Locale.ENGLISH),
				this.filterOption.getCurrent());
	}

	/**
	 * Sets the "Toggle Craftable" Button texture.
	 * 
	 * No-op as we don't use the button.
	 */
	@Override
	protected void setBookButtonTexture() {}

	/**
	 * No-op.
	 */
	@Override
	protected boolean isValid(Slot slot) {
		return false;
	}

	/**
	 * No-op.
	 */
	@Override
	protected void populateRecipes(RecipeResultCollection recipeResultCollection, RecipeFinder recipeFinder) {}

	/**
	 * No-op since we don't show the button.
	 */
	@Override
	protected Text getToggleCraftableButtonText() {
		return null;
	}

	/**
	 * No-op.
	 */
	@Override
	protected void showGhostRecipe(GhostRecipe ghostRecipe, RecipeDisplay display, ContextParameterMap context) {}

	/**
	 * No-op. Prevents a crash.
	 */
	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {}
}
