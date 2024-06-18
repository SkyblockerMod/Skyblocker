package de.hysky.skyblocker.skyblock.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemListTab extends ItemListWidget.TabContainerWidget {

    private SearchResultsWidget results;
    private final MinecraftClient client;
    private TextFieldWidget searchField;

    public ItemListTab(int x, int y, MinecraftClient client, TextFieldWidget searchField) {
        super(x, y, Text.literal("Item List Tab"));
        this.client = client;
        this.searchField = searchField;
        if (ItemRepository.filesImported()) {
            this.results = new SearchResultsWidget(this.client, x - 9, y - 9 );
            this.results.updateSearchResult(searchField == null ? "": this.searchField.getText());
        }
    }

    @Override
    public List<? extends Element> children() {
        return List.of(results, searchField);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0.0D, 0.0D, 100.0D);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = getX();
        int y = getY();

        // all coordinates offseted -9
        if (!ItemRepository.filesImported() && !this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
            Text hintText = (Text.literal("Loading...")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
            context.drawTextWithShadow(this.client.textRenderer, hintText, x + 16, y + 7, -1);
        } else if (!this.searchField.isFocused() && this.searchField.getText().isEmpty()) {
            Text hintText = (Text.translatable("gui.recipebook.search_hint")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
            context.drawTextWithShadow(this.client.textRenderer, hintText, x + 16, y + 7, -1);
        } else {
            this.searchField.render(context, mouseX, mouseY, delta);
        }
        if (ItemRepository.filesImported()) {
            if (results == null) {
                this.results = new SearchResultsWidget(this.client, x - 9, y - 9);
            }
            this.results.updateSearchResult(this.searchField.getText());
            this.results.render(context, mouseX, mouseY, delta);
        }
        matrices.pop();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void setSearchField(TextFieldWidget searchField) {
        this.searchField = searchField;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        if (this.searchField.mouseClicked(mouseX, mouseY, button) && this.results != null) {
            this.results.closeRecipeView();
            this.searchField.setFocused(true);
            return true;
        } else if (results != null) {
            this.searchField.setFocused(false);
            this.results.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        if (this.results != null) this.results.drawTooltip(context, mouseX, mouseY);
    }
}
