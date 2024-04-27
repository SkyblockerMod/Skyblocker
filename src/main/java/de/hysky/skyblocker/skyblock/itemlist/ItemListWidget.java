package de.hysky.skyblocker.skyblock.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.mixins.accessors.RecipeBookWidgetAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(value = EnvType.CLIENT)
public class ItemListWidget extends RecipeBookWidget {
    private int parentWidth;
    private int parentHeight;
    private int leftOffset;
    private TextFieldWidget searchField;
    private SearchResultsWidget results;

    public ItemListWidget() {
        super();
    }

    public void updateSearchResult() {
        this.results.updateSearchResult(((RecipeBookWidgetAccessor) this).getSearchText());
    }

    @Override
    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow, AbstractRecipeScreenHandler<?> craftingScreenHandler) {
        super.initialize(parentWidth, parentHeight, client, narrow, craftingScreenHandler);
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.leftOffset = narrow ? 0 : 86;
        this.searchField = ((RecipeBookWidgetAccessor) this).getSearchField();
        int x = (this.parentWidth - 147) / 2 - this.leftOffset;
        int y = (this.parentHeight - 166) / 2;
        if (ItemRepository.filesImported()) {
            this.results = new SearchResultsWidget(this.client, x, y);
            this.updateSearchResult();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isOpen()) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(0.0D, 0.0D, 100.0D);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.searchField = ((RecipeBookWidgetAccessor) this).getSearchField();
            int i = (this.parentWidth - 147) / 2 - this.leftOffset;
            int j = (this.parentHeight - 166) / 2;
            context.drawTexture(TEXTURE, i, j, 1, 1, 147, 166);
            this.searchField = ((RecipeBookWidgetAccessor) this).getSearchField();

            if (!ItemRepository.filesImported() && !this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
                Text hintText = (Text.literal("Loading...")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
                context.drawTextWithShadow(this.client.textRenderer, hintText, i + 25, j + 14, -1);
            } else if (!this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
                Text hintText = (Text.translatable("gui.recipebook.search_hint")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
                context.drawTextWithShadow(this.client.textRenderer, hintText, i + 25, j + 14, -1);
            } else {
                this.searchField.render(context, mouseX, mouseY, delta);
            }
            if (ItemRepository.filesImported()) {
                if (results == null) {
                    int x = (this.parentWidth - 147) / 2 - this.leftOffset;
                    int y = (this.parentHeight - 166) / 2;
                    this.results = new SearchResultsWidget(this.client, x, y);
                }
                this.updateSearchResult();
                this.results.render(context, mouseX, mouseY, delta);
            }
            matrices.pop();
        }
    }

    @Override
    public void drawTooltip(DrawContext context, int x, int y, int mouseX, int mouseY) {
        if (this.isOpen() && ItemRepository.filesImported() && results != null) {
            this.results.drawTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isOpen() && this.client.player != null && !this.client.player.isSpectator() && ItemRepository.filesImported() && this.searchField != null && results != null) {
            if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
                this.results.closeRecipeView();
                this.searchField.setFocused(true);
                return true;
            } else {
                this.searchField.setFocused(false);
                return this.results.mouseClicked(mouseX, mouseY, button);
            }
        } else return false;
    }
}