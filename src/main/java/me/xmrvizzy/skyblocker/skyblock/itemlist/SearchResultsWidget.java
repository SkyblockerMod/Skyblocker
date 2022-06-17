package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchResultsWidget implements Drawable {
    private static final Identifier TEXTURE = new Identifier("textures/gui/recipe_book.png");

    private final MinecraftClient client;
    private final int parentX;
    private final int parentY;
    private final int rows = 4;
    private final int cols = 5;

    private final List<ItemStack> searchResults = new ArrayList<>();
    private List<Recipe> recipeResults = new ArrayList<>();
    private String searchText = null;
    private List<ResultButtonWidget> resultButtons = new ArrayList<>();
    private ToggleButtonWidget nextPageButton;
    private ToggleButtonWidget prevPageButton;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean displayRecipes = false;

    public SearchResultsWidget(MinecraftClient client, int parentX, int parentY) {
        this.client = client;
        this.parentX = parentX;
        this.parentY = parentY;
        int gridX = parentX + 11;
        int gridY = parentY + 31;
        for (int i = 0; i < this.rows; ++i)
            for (int j = 0; j < this.cols; ++j) {
                int x = gridX + j * 25;
                int y = gridY + i * 25;
                resultButtons.add(new ResultButtonWidget(x, y));
            }
        this.nextPageButton = new ToggleButtonWidget(parentX + 93, parentY + 137, 12, 17, false);
        this.nextPageButton.setTextureUV(1, 208, 13, 18, TEXTURE);
        this.prevPageButton = new ToggleButtonWidget(parentX + 38, parentY + 137, 12, 17, true);
        this.prevPageButton.setTextureUV(1, 208, 13, 18, TEXTURE);
    }

    public void closeRecipeView() {
        this.currentPage = 0;
        this.pageCount = (this.searchResults.size() - 1) / resultButtons.size() + 1;
        this.displayRecipes = false;
        this.updateButtons();
    }

    protected void updateSearchResult(String searchText) {
        if (!searchText.equals(this.searchText)) {
            this.searchText = searchText;
            this.searchResults.clear();
            for (ItemStack entry : ItemRegistry.items) {
                String name = entry.getName().toString().toLowerCase(Locale.ENGLISH);
                String disp = entry.getNbt().getCompound("display").toString().toLowerCase(Locale.ENGLISH);
                if (name.contains(this.searchText) || disp.contains(this.searchText))
                    this.searchResults.add(entry);
            }
            this.currentPage = 0;
            this.pageCount = (this.searchResults.size() - 1) / resultButtons.size() + 1;
            this.displayRecipes = false;
            this.updateButtons();
        }
    }

    private void updateButtons() {
        if (this.displayRecipes) {
            Recipe recipe = this.recipeResults.get(this.currentPage);
            for (ResultButtonWidget button : resultButtons)
                button.clearItemStack();
            resultButtons.get( 5).setItemStack(recipe.grid.get(0));
            resultButtons.get( 6).setItemStack(recipe.grid.get(1));
            resultButtons.get( 7).setItemStack(recipe.grid.get(2));
            resultButtons.get(10).setItemStack(recipe.grid.get(3));
            resultButtons.get(11).setItemStack(recipe.grid.get(4));
            resultButtons.get(12).setItemStack(recipe.grid.get(5));
            resultButtons.get(15).setItemStack(recipe.grid.get(6));
            resultButtons.get(16).setItemStack(recipe.grid.get(7));
            resultButtons.get(17).setItemStack(recipe.grid.get(8));
            resultButtons.get(14).setItemStack(recipe.result);
        } else {
            for (int i = 0; i < resultButtons.size(); ++i) {
                int index = this.currentPage * resultButtons.size() + i;
                if (index < this.searchResults.size()) {
                    resultButtons.get(i).setItemStack(this.searchResults.get(index));
                } else {
                    resultButtons.get(i).clearItemStack();
                }
            }
        }
        this.prevPageButton.active = this.currentPage > 0;
        this.nextPageButton.active = this.currentPage < this.pageCount - 1;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.disableDepthTest();
        if (this.displayRecipes) {
            String craftText = this.recipeResults.get(this.currentPage).text;
            this.client.textRenderer.drawWithShadow(matrices, craftText, this.parentX + 11, this.parentY + 31, 0xffffffff);
            Text resultText = this.recipeResults.get(this.currentPage).result.getName();
            this.client.textRenderer.drawWithShadow(matrices, resultText, this.parentX + 11, this.parentY + 43, 0xffffffff);
            this.client.textRenderer.drawWithShadow(matrices, "▶", this.parentX + 96, this.parentY + 90, 0xaaffffff);
        }
        for (ResultButtonWidget button : resultButtons)
            button.render(matrices, mouseX, mouseY, delta);
        if (this.pageCount > 1) {
            String string = (this.currentPage + 1) + "/" + this.pageCount;
            int dx = this.client.textRenderer.getWidth(string) / 2;
            this.client.textRenderer.draw(matrices, string, this.parentX - dx + 73, this.parentY + 141, -1);
        }
        if (this.prevPageButton.active) this.prevPageButton.render(matrices, mouseX, mouseY, delta);
        if (this.nextPageButton.active) this.nextPageButton.render(matrices, mouseX, mouseY, delta);
        RenderSystem.enableDepthTest();
    }

    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        for (ResultButtonWidget button : resultButtons)
            if (button.isMouseOver(mouseX, mouseY))
                button.renderTooltip(matrices, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (ResultButtonWidget button : resultButtons)
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                String internalName = button.itemStack.getNbt().getCompound("ExtraAttributes").getString("id");
                List<Recipe> recipes = ItemRegistry.getRecipes(internalName);
                if (!recipes.isEmpty()) {
                    this.recipeResults = recipes;
                    this.currentPage = 0;
                    this.pageCount = recipes.size();
                    this.displayRecipes = true;
                    this.updateButtons();
                }
                return true;
            }
        if (this.prevPageButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            --this.currentPage;
            this.updateButtons();
            return true;
        }
        if (this.nextPageButton.mouseClicked(mouseX, mouseY, mouseButton)) {
            ++this.currentPage;
            this.updateButtons();
            return true;
        }
        return false;
    }

}
