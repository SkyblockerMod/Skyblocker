package de.hysky.skyblocker.skyblock.searchOverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class OverlayScreen extends Screen {

    protected static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");

    private TextFieldWidget searchField;
    private ButtonWidget finishedButton;
    private ButtonWidget[] suggestionButtons;
    private ButtonWidget[] historyButtons;



    public OverlayScreen(Text title) {
        super(title);
    }
    @Override
    protected void init() {
        super.init();
        int rowHeight = 20;
        int rowWidth = (int)(this.width * 0.33);

        int startX = (int)(this.width * 0.5) - rowWidth/2;
        int startY = (int) ((int)(this.height * 0.5)- (rowHeight * (1+ SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions + 0.75 + SkyblockerConfigManager.get().general.searchOverlay.historyLength)) /2);

        // Search field
        this.searchField = new TextFieldWidget(textRenderer,   startX,  startY, rowWidth - rowHeight, rowHeight, Text.literal("Search..."));
        searchField.setChangedListener(SearchOverManager::updateSearch);
        searchField.setMaxLength(30);


        // finish buttons
        finishedButton = ButtonWidget.builder(Text.literal("").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), (a) -> {
                    close();
                })
                .position(startX + rowWidth - rowHeight, startY)
                .size(rowHeight, rowHeight).build();
        // suggested item buttons
        int rowOffset = rowHeight;
        int totalSuggestions = SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions;
        this.suggestionButtons = new ButtonWidget[totalSuggestions];
        for (int i = 0; i < totalSuggestions; i++) {
            suggestionButtons[i] = ButtonWidget.builder(Text.literal(SearchOverManager.getSuggestion(i)).setStyle(Style.EMPTY), (a) -> {
                        SearchOverManager.updateSearch(a.getMessage().getString());
                        close();
                    })
                    .position(startX , startY + rowOffset)
                    .size(rowWidth, rowHeight).build();
            suggestionButtons[i].visible = false;
            rowOffset += rowHeight;
        }
        // history item buttons
        rowOffset += (int) (rowHeight * 0.75);
        int historyLength = SkyblockerConfigManager.get().general.searchOverlay.historyLength; //todo look different
        this.historyButtons = new ButtonWidget[historyLength];
        for (int i = 0; i < historyLength; i++) {
            String text = SearchOverManager.getHistory(i);
            if (text != null){
                historyButtons[i] = ButtonWidget.builder(Text.literal(text).setStyle(Style.EMPTY), (a) -> {
                            SearchOverManager.updateSearch(a.getMessage().getString());
                            close();
                        })
                        .position(startX , startY + rowOffset)
                        .size(rowWidth, rowHeight).build();
                rowOffset += rowHeight;
            }else{
                break;
            }
        }

        addDrawableChild(searchField);
        for (ButtonWidget suggestion : suggestionButtons){
            addDrawableChild(suggestion);
        }
        for (ButtonWidget historyOption : historyButtons){
            if (historyOption != null){
                addDrawableChild(historyOption);
            }
        }
        addDrawableChild(finishedButton);

        this.setInitialFocus(searchField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, finishedButton.getX() + 2, finishedButton.getY() + 2, 16, 16); //todo rowHeight -4
        if(historyButtons.length > 0  && historyButtons[0] != null){
            context.drawText(textRenderer, "History:", historyButtons[0].getX()+2, historyButtons[0].getY() - 10, 0xFFFFFFFF, true); //todo load form en_us and rowHeight
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        //todo draw custom background
       
    }

    @Override
    public void close() {
        assert this.client != null;
        assert this.client.player != null;
        SearchOverManager.pushSearch();
        super.close();
    }
    @Override
    public final void tick() {
        super.tick();
        //update suggestion buttons text
        for (int i = 0; i < SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions; i++) {
            String text = SearchOverManager.getSuggestion(i);
            if (!Objects.equals(text, "")){
                suggestionButtons[i].visible = true;
                suggestionButtons[i].setMessage(Text.literal(text).setStyle(Style.EMPTY));
            }else{
                suggestionButtons[i].visible = false;
            }

        }

    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode,scanCode,modifiers);
        //if enter key pressed and search box selected close
        if (keyCode == GLFW.GLFW_KEY_ENTER && searchField.isActive()){
            close();
            return true;
        }
        return false;
    }

}
