package de.hysky.skyblocker.skyblock.searchoverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import dev.isxander.yacl3.gui.TooltipButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static de.hysky.skyblocker.skyblock.itemlist.ItemRepository.getItemStack;

public class OverlayScreen extends Screen {

    protected static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");
    private static final int rowHeight = 20;
    private TextFieldWidget searchField;
    private ButtonWidget finishedButton;
    private ButtonWidget maxPetButton;
    private TextFieldWidget dungeonStarField;
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
        finishedButton = ButtonWidget.builder(Text.literal("").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), a -> close())
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
            maxPetButton = ButtonWidget.builder(Text.literal("temp"), a -> {
                        SearchOverManager.maxPetLevel = !SearchOverManager.maxPetLevel;
                        if (SearchOverManager.maxPetLevel) {
                            maxPetButton.setMessage(Text.literal("Max Pet Level ✔").formatted(Formatting.GREEN));
                        } else {
                            maxPetButton.setMessage(Text.literal("Max Pet Level ❌").formatted(Formatting.RED));
                        }
                    })
                    .position(startX + rowWidth, startY)
                    .size(rowWidth / 2, rowHeight).build();
            if (SearchOverManager.maxPetLevel) {
                maxPetButton.setMessage(Text.literal("Max Pet Level ✔").formatted(Formatting.GREEN));
            } else {
                maxPetButton.setMessage(Text.literal("Max Pet Level ❌").formatted(Formatting.RED));
            }

            //dungeon star input
            this.dungeonStarField = new TextFieldWidget(textRenderer, startX + (int) (rowWidth * 1.25), startY + rowHeight, rowWidth / 4, rowHeight, Text.literal(""));
            if (SearchOverManager.dungeonStars > 0) {
                dungeonStarField.setText(String.valueOf(SearchOverManager.dungeonStars));
            }
            dungeonStarField.setMaxLength(2);
            dungeonStarField.setTooltip(Tooltip.of(Text.of("Star count for dungeon items")));
            dungeonStarField.setChangedListener(SearchOverManager::updateDungeonStars);
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
            addDrawableChild(dungeonStarField);
        }

        //focus the search box
        this.setInitialFocus(searchField);
    }

    /**
     * Renders the search icon, label for the history and item Stacks for item names
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int renderOffset = (rowHeight - 16) / 2;
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, finishedButton.getX() + renderOffset, finishedButton.getY() + renderOffset, 16, 16);
        //labels
        if (historyButtons.length > 0 && historyButtons[0] != null) {
            context.drawText(textRenderer, Text.translatable("skyblocker.config.general.searchOverlay.historyLabel"), historyButtons[0].getX() + renderOffset, historyButtons[0].getY() - rowHeight / 2, 0xFFFFFFFF, true);
        }
        if (SearchOverManager.isAuction) {
            context.drawText(textRenderer, Text.literal("Stars:"), dungeonStarField.getX() - dungeonStarField.getWidth() + renderOffset, dungeonStarField.getY() + rowHeight / 4, 0xFFFFFFFF, true);
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
