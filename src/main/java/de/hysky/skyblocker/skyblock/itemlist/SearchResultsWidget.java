package de.hysky.skyblocker.skyblock.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsWidget implements Drawable {
    private static final ButtonTextures PAGE_FORWARD_TEXTURES = new ButtonTextures(new Identifier("recipe_book/page_forward"), new Identifier("recipe_book/page_forward_highlighted"));
    private static final ButtonTextures PAGE_BACKWARD_TEXTURES = new ButtonTextures(new Identifier("recipe_book/page_backward"), new Identifier("recipe_book/page_backward_highlighted"));
    private static final int COLS = 5;
    private static final int MAX_TEXT_WIDTH = 124;
    private static final String ELLIPSIS = "...";
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private final MinecraftClient client;
    private final int parentX;
    private final int parentY;

    private final List<ItemStack> searchResults = new ArrayList<>();
    private List<SkyblockCraftingRecipe> recipeResults = new ArrayList<>();
    private String searchText = null;
    private final List<ResultButtonWidget> resultButtons = new ArrayList<>();
    private final ToggleButtonWidget nextPageButton;
    private final ToggleButtonWidget prevPageButton;
    private int currentPage = 0;
    private int pageCount = 0;
    private boolean displayRecipes = false;

    public SearchResultsWidget(MinecraftClient client, int parentX, int parentY) {
        this.client = client;
        this.parentX = parentX;
        this.parentY = parentY;
        int gridX = parentX + 11;
        int gridY = parentY + 31;
        int rows = 4;
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < COLS; ++j) {
                int x = gridX + j * 25;
                int y = gridY + i * 25;
                resultButtons.add(new ResultButtonWidget(x, y));
            }
        this.nextPageButton = new ToggleButtonWidget(parentX + 93, parentY + 137, 12, 17, false);
        this.nextPageButton.setTextures(PAGE_FORWARD_TEXTURES);
        this.prevPageButton = new ToggleButtonWidget(parentX + 38, parentY + 137, 12, 17, true);
        this.prevPageButton.setTextures(PAGE_BACKWARD_TEXTURES);
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
            for (ItemStack entry : ItemRepository.getItems()) {
                String name = entry.getName().toString().toLowerCase(Locale.ENGLISH);
                LoreComponent lore = entry.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
                if (name.contains(this.searchText) || lore.lines().stream().map(Text::getString).anyMatch(s -> s.contains(this.searchText)))
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
            SkyblockCraftingRecipe recipe = this.recipeResults.get(this.currentPage);
            for (ResultButtonWidget button : resultButtons)
                button.clearItemStack();
            resultButtons.get(5).setItemStack(recipe.getGrid().get(0));
            resultButtons.get(6).setItemStack(recipe.getGrid().get(1));
            resultButtons.get(7).setItemStack(recipe.getGrid().get(2));
            resultButtons.get(10).setItemStack(recipe.getGrid().get(3));
            resultButtons.get(11).setItemStack(recipe.getGrid().get(4));
            resultButtons.get(12).setItemStack(recipe.getGrid().get(5));
            resultButtons.get(15).setItemStack(recipe.getGrid().get(6));
            resultButtons.get(16).setItemStack(recipe.getGrid().get(7));
            resultButtons.get(17).setItemStack(recipe.getGrid().get(8));
            resultButtons.get(14).setItemStack(recipe.getResult());
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

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        RenderSystem.disableDepthTest();
        if (this.displayRecipes) {
            //Craft text - usually a requirement for the recipe
            String craftText = this.recipeResults.get(this.currentPage).getCraftText();
            if (textRenderer.getWidth(craftText) > MAX_TEXT_WIDTH) {
            	drawTooltip(textRenderer, context, craftText, this.parentX + 11, this.parentY + 31, mouseX, mouseY);
            	craftText = textRenderer.trimToWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS;
            }
            context.drawTextWithShadow(textRenderer, craftText, this.parentX + 11, this.parentY + 31, 0xffffffff);

            //Item name
            Text resultText = this.recipeResults.get(this.currentPage).getResult().getName();
            if (textRenderer.getWidth(Formatting.strip(resultText.getString())) > MAX_TEXT_WIDTH) {
            	drawTooltip(textRenderer, context, resultText, this.parentX + 11, this.parentY + 43, mouseX, mouseY);
            	resultText = Text.literal(getLegacyFormatting(resultText.getString()) + textRenderer.trimToWidth(Formatting.strip(resultText.getString()), MAX_TEXT_WIDTH) + ELLIPSIS).setStyle(resultText.getStyle());
            }
            context.drawTextWithShadow(textRenderer, resultText, this.parentX + 11, this.parentY + 43, 0xffffffff);

            //Arrow pointing to result item from the recipe
            context.drawTextWithShadow(textRenderer, "▶", this.parentX + 96, this.parentY + 90, 0xaaffffff);
        }
        for (ResultButtonWidget button : resultButtons)
            button.render(context, mouseX, mouseY, delta);
        if (this.pageCount > 1) {
            String string = (this.currentPage + 1) + "/" + this.pageCount;
            int dx = this.client.textRenderer.getWidth(string) / 2;
            context.drawText(textRenderer, string, this.parentX - dx + 73, this.parentY + 141, -1, false);
        }
        if (this.prevPageButton.active) this.prevPageButton.render(context, mouseX, mouseY, delta);
        if (this.nextPageButton.active) this.nextPageButton.render(context, mouseX, mouseY, delta);
        RenderSystem.enableDepthTest();
    }

    /**
     * Used for drawing tooltips over truncated text
     */
    private void drawTooltip(TextRenderer textRenderer, DrawContext context, Text text, int textX, int textY, int mouseX, int mouseY){
        RenderSystem.disableDepthTest();
            if (mouseX >= textX && mouseX <= textX + MAX_TEXT_WIDTH + 4 && mouseY >=  textY && mouseY <= textY + 9) {
                context.drawTooltip(textRenderer, text, mouseX, mouseY);
            }
        RenderSystem.enableDepthTest();
    }

    /**
     * @see #drawTooltip(TextRenderer, DrawContext, Text, int, int, int, int)
     */
    private void drawTooltip(TextRenderer textRenderer, DrawContext context, String text, int textX, int textY, int mouseX, int mouseY){
        drawTooltip(textRenderer, context, Text.of(text), textX, textY, mouseX, mouseY);
    }

    /**
     * Retrieves the first occurrence of section symbol formatting in a string
     * 
     * @param string The string to fetch section symbol formatting from
     * @return The section symbol and its formatting code or {@code null} if a match isn't found or if the {@code string} is null
     */
    private static String getLegacyFormatting(@Nullable String string) {
        if (string == null) {
            return null;
        }
        Matcher matcher = FORMATTING_CODE_PATTERN.matcher(string);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        for (ResultButtonWidget button : resultButtons)
            if (button.isMouseOver(mouseX, mouseY))
                button.renderTooltip(context, mouseX, mouseY);
        RenderSystem.enableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (ResultButtonWidget button : resultButtons)
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                String internalName = ItemUtils.getItemId(button.itemStack);
                if (internalName.isEmpty()) {
                    continue;
                }
                List<SkyblockCraftingRecipe> recipes = ItemRepository.getRecipes(internalName);
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
