package de.hysky.skyblocker.skyblock.searchoverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static de.hysky.skyblocker.skyblock.itemlist.ItemRepository.getItemStack;

public class OverlayScreen extends Screen {

    protected static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("social_interactions/background");
    private static final int rowHeight = 20;
    private TextFieldWidget searchField;
    private ButtonWidget finishedButton;
    private ButtonWidget maxPetButton;
    private ButtonWidget dungeonStarButton;
    private ButtonWidget[] suggestionButtons;
    private ButtonWidget[] historyButtons;

    public OverlayScreen(Text title) {
        super(title);
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
        this.searchField = new TextFieldWidget(textRenderer, startX, startY, rowWidth - rowHeight, rowHeight, Text.translatable("gui.recipebook.search_hint"));
        searchField.setText(SearchOverManager.search);
        searchField.setChangedListener(SearchOverManager::updateSearch);
        searchField.setMaxLength(30);

        // finish buttons
        finishedButton = ButtonWidget.builder(Text.literal(""), a -> close())
                .position(startX + rowWidth - rowHeight, startY)
                .size(rowHeight, rowHeight).build();


        // suggested item buttons
        int rowOffset = rowHeight;
        int totalSuggestions = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.maxSuggestions;
        this.suggestionButtons = new ButtonWidget[totalSuggestions];
        for (int i = 0; i < totalSuggestions; i++) {
            suggestionButtons[i] = ButtonWidget.builder(Text.literal(SearchOverManager.getSuggestion(i)).setStyle(Style.EMPTY), a -> {
                        SearchOverManager.updateSearch(a.getMessage().getString());
                        close();
                    })
                    .position(startX, startY + rowOffset)
                    .size(rowWidth, rowHeight).build();
            suggestionButtons[i].visible = false;
            rowOffset += rowHeight;
        }
        // history item buttons
        rowOffset += (int) (rowHeight * 0.75);
        int historyLength = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.historyLength;
        this.historyButtons = new ButtonWidget[historyLength];
        for (int i = 0; i < historyLength; i++) {
            String text = SearchOverManager.getHistory(i);
            if (text != null) {
                historyButtons[i] = ButtonWidget.builder(Text.literal(text).setStyle(Style.EMPTY), (a) -> {
                            SearchOverManager.updateSearch(a.getMessage().getString());
                            close();
                        })
                        .position(startX, startY + rowOffset)
                        .size(rowWidth, rowHeight).build();
                rowOffset += rowHeight;
            } else {
                break;
            }
        }
        //auction only elements
        if (SearchOverManager.isAuction) {
            //max pet level button
            maxPetButton = ButtonWidget.builder(Text.literal(""), a -> {
                        SearchOverManager.maxPetLevel = !SearchOverManager.maxPetLevel;
                        updateMaxPetText();
                    })
                    .tooltip(Tooltip.of(Text.translatable("skyblocker.config.general.searchOverlay.maxPet.@Tooltip")))
                    .position(startX, startY - rowHeight - 8)
                    .size(rowWidth / 2, rowHeight).build();
            updateMaxPetText();

            //dungeon star input
            dungeonStarButton = ButtonWidget.builder(Text.literal("✪"), a -> updateStars())
                    .tooltip(Tooltip.of(Text.translatable("skyblocker.config.general.searchOverlay.starsTooltip")))
                    .position(startX + (int) (rowWidth * 0.5), startY - rowHeight - 8)
                    .size(rowWidth / 2, rowHeight).build();

            updateStars();
        }

        //add drawables in order to make tab navigation sensible
        addDrawableChild(searchField);
        for (ButtonWidget suggestion : suggestionButtons) {
            addDrawableChild(suggestion);
        }
        for (ButtonWidget historyOption : historyButtons) {
            if (historyOption != null) {
                addDrawableChild(historyOption);
            }
        }
        addDrawableChild(finishedButton);

        if (SearchOverManager.isAuction) {
            addDrawableChild(maxPetButton);
            addDrawableChild(dungeonStarButton);
        }

        //focus the search box
        this.setInitialFocus(searchField);
    }

    /**
     * Finds if the mouse is clicked on the dungeon star button and if so works out what stars the user clicked on
     *
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     * @param button the mouse button number
     * @return super
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (SearchOverManager.isAuction && dungeonStarButton.isHovered() && client != null) {
            double actualTextWidth = client.textRenderer.getWidth(dungeonStarButton.getMessage());
            double textOffset = (dungeonStarButton.getWidth() - actualTextWidth) / 2;
            double offset = mouseX - (dungeonStarButton.getX() + textOffset);
            int starCount = (int) ((offset / actualTextWidth) * 10);
            starCount = Math.clamp(starCount + 1, 0, 10);
            //if same as old value set stars to 0 else set to selected amount
            if (starCount == SearchOverManager.dungeonStars) {
                SearchOverManager.dungeonStars = 0;
            } else {
                SearchOverManager.dungeonStars = starCount;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Updates the text displayed on the max pet level button to represent the settings current state
     */
    private void updateMaxPetText() {
        if (SearchOverManager.maxPetLevel) {
            maxPetButton.setMessage(Text.translatable("skyblocker.config.general.searchOverlay.maxPet").append(Text.literal(" ✔")).formatted(Formatting.GREEN));
        } else {
            maxPetButton.setMessage(Text.translatable("skyblocker.config.general.searchOverlay.maxPet").append(Text.literal(" ❌")).formatted(Formatting.RED));
        }
    }

    /**
     * Updates stars in dungeon star input to represent the current star value
     */
    private void updateStars() {
        MutableText stars = Text.empty();
        for (int i = 0; i < SearchOverManager.dungeonStars; i++) {
            stars.append(Text.literal("✪").formatted(i < 5 ? Formatting.YELLOW : Formatting.RED));
        }
        for (int i = SearchOverManager.dungeonStars; i < 10; i++) {
            stars.append(Text.literal("✪"));
        }
        dungeonStarButton.setMessage(stars);
    }

    /**
     * Renders the background for the search using the social interactions background
     * @param context context
     * @param mouseX mouseX
     * @param mouseY mouseY
     * @param delta delta
     */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        //find max height
        int maxHeight = rowHeight * (1 + suggestionButtons.length + historyButtons.length);
        if (historyButtons.length > 0) { //add space for history label if it could exist
            maxHeight += (int) (rowHeight * 0.75);
        }
        context.drawGuiTexture(BACKGROUND_TEXTURE, searchField.getX() - 8, searchField.getY() - 8, (int) (this.width * 0.4) + 16, maxHeight + 16);
    }

    /**
     * Renders the search icon, label for the history and item Stacks for item names
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int renderOffset = (rowHeight - 16) / 2;
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, finishedButton.getX() + renderOffset, finishedButton.getY() + renderOffset, 16, 16);
        if (historyButtons.length > 0 && historyButtons[0] != null) {
            context.drawText(textRenderer, Text.translatable("skyblocker.config.general.searchOverlay.historyLabel"), historyButtons[0].getX() + renderOffset, historyButtons[0].getY() - rowHeight / 2, 0xFFFFFFFF, true);
        }

        //draw item stacks and tooltip to buttons
        for (int i = 0; i < suggestionButtons.length; i++) {
            drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getSuggestionId(i), suggestionButtons[i], renderOffset);
        }
        for (int i = 0; i < historyButtons.length; i++) {
            drawItemAndTooltip(context, mouseX, mouseY, SearchOverManager.getHistoryId(i), historyButtons[i], renderOffset);
        }
    }

    /**
     * Draws the item and tooltip for the given button
     */
    private void drawItemAndTooltip(DrawContext context, int mouseX, int mouseY, String id, ButtonWidget button, int renderOffset) {
        if (id == null || id.isEmpty()) return;
        ItemStack item = getItemStack(id);
        if (item == null) return;
        context.drawItem(item, button.getX() + renderOffset, button.getY() + renderOffset);

        // Draw tooltip
        if (button.isMouseOver(mouseX, mouseY)) {
            context.drawItemTooltip(textRenderer, item, mouseX, mouseY);
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

                suggestionButtons[i].setMessage(Text.literal(text).setStyle(Style.EMPTY));
            } else {
                suggestionButtons[i].visible = false;
            }
        }
    }

    /**
     * When a key is pressed. If enter key pressed and search box selected close
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && searchField.isActive()) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /**
     * Closes the overlay screen and gets the manager to send a packet update about the sign
     */
    @Override
    public void close() {
        assert this.client != null;
        assert this.client.player != null;
        SearchOverManager.pushSearch();
        super.close();
    }
}
