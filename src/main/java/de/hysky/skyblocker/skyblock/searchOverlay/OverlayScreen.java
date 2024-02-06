package de.hysky.skyblocker.skyblock.searchOverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class OverlayScreen extends Screen {

    protected static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");

    private TextFieldWidget searchField;
    private ButtonWidget finishedButton;
    private ButtonWidget[] suggestionButtons;
    //todo history buttons

    public OverlayScreen(Text title) {
        super(title);
    }
    @Override
    protected void init() {
        super.init();
        int rowHeight = 20;
        int rowWidth = (int)(this.width * 0.33);

        int startX = (int)(this.width * 0.5) - rowWidth/2;
        int startY = (int)(this.height * 0.5)- (rowHeight * (1+ SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions)) /2;

        // Search field
        this.searchField = new TextFieldWidget(textRenderer,   startX,  startY, rowWidth - 30, rowHeight, Text.literal("Search..."));
        searchField.setChangedListener(SearchOverManager::updateSearch);
        searchField.setFocused(true);
        searchField.active = true;

        // finish buttons
        finishedButton = ButtonWidget.builder(Text.literal("SEARCH").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), (a) -> { //todo search icon
                    close();
                })
                .position(startX + rowWidth - 30, startY)
                .size(30, rowHeight).build();
        // suggested item buttons
        int totalSuggestions = SkyblockerConfigManager.get().general.searchOverlay.maxSuggestions;
        this.suggestionButtons = new ButtonWidget[totalSuggestions];
        for (int i = 0; i < totalSuggestions; i++) {
            suggestionButtons[i] = ButtonWidget.builder(Text.literal(SearchOverManager.getSuggestion(i)).setStyle(Style.EMPTY), (a) -> {
                        SearchOverManager.updateSearch(a.getMessage().getString());
                        close();
                    })
                    .position(startX , startY + rowHeight * (i+1))
                    .size(rowWidth, rowHeight).build();
            suggestionButtons[i].visible = false;
        }

        addDrawableChild(searchField);
        for (ButtonWidget suggestion : suggestionButtons){
            addDrawableChild(suggestion);
        }
        addDrawableChild(finishedButton);



    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
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
}
