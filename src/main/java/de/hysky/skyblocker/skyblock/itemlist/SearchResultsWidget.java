package de.hysky.skyblocker.skyblock.itemlist;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Deprecated(forRemoval = true)
public class SearchResultsWidget implements Drawable, Element {
    private static final ButtonTextures PAGE_FORWARD_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/page_forward"), Identifier.ofVanilla("recipe_book/page_forward_highlighted"));
    private static final ButtonTextures PAGE_BACKWARD_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/page_backward"), Identifier.ofVanilla("recipe_book/page_backward_highlighted"));
    private static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    private static final Identifier ARROW_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "arrow");
    private static final int COLS = 5;
    private static final int MAX_TEXT_WIDTH = 124;
    private static final String ELLIPSIS = "...";

    private final MinecraftClient client;
    private final int parentX;
    private final int parentY;

    private final List<ItemStack> searchResults = new ArrayList<>();
    private List<SkyblockRecipe> recipeResults = new ArrayList<>();
    private String searchText = null;
    private final List<ResultButtonWidget> resultButtons = new ArrayList<>();
    private final List<SkyblockRecipe.RecipeSlot> recipeSlots = new ArrayList<>();
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
        searchText = searchText.toLowerCase(Locale.ENGLISH);

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
        recipeSlots.clear();
        if (this.displayRecipes) {
            SkyblockRecipe recipe = this.recipeResults.get(this.currentPage);
            for (ResultButtonWidget button : resultButtons)
                button.clearItemStack();
            recipeSlots.addAll(recipe.getInputSlots(125, 75));
            recipeSlots.addAll(recipe.getOutputSlots(125, 75));
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
            SkyblockRecipe recipe = this.recipeResults.get(this.currentPage);
            Text craftText = recipe.getExtraText();
            if (textRenderer.getWidth(craftText) > MAX_TEXT_WIDTH) {
            	drawTooltip(textRenderer, context, craftText, this.parentX + 11, this.parentY + 31 + 100 - 9, mouseX, mouseY);
            	craftText = Text.of(textRenderer.trimToWidth(craftText, MAX_TEXT_WIDTH) + ELLIPSIS);
            }
            context.drawTextWithShadow(textRenderer, craftText, this.parentX + 11, this.parentY + 31 + 100 - 9, 0xffffffff);

            // Recipe category
            Text category = Text.translatable("emi.category.skyblocker." + recipe.getCategoryIdentifier().getPath());
            context.drawTextWithShadow(textRenderer, category, this.parentX + 11, this.parentY + 31, -1);

            //Item name
            // TODO fix if we add recipes with multiple outputs
            Text resultText = recipe.getOutputs().getFirst().getName();
            if (textRenderer.getWidth(resultText) > MAX_TEXT_WIDTH) {
            	drawTooltip(textRenderer, context, resultText, this.parentX + 11, this.parentY + 43, mouseX, mouseY);
            	StringVisitable trimmed = StringVisitable.concat(textRenderer.trimToWidth(resultText, MAX_TEXT_WIDTH), StringVisitable.plain(ELLIPSIS));
            	OrderedText ordered = Language.getInstance().reorder(trimmed);

            	context.drawTextWithShadow(textRenderer, ordered, this.parentX + 11, this.parentY + 43, 0xffffffff);
            } else {
            	context.drawTextWithShadow(textRenderer, resultText, this.parentX + 11, this.parentY + 43, 0xffffffff);
            }

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(parentX + 11, parentY + 31 + 25, 0);
            for (SkyblockRecipe.RecipeSlot recipeSlot : recipeSlots) {
                if (recipeSlot.showBackground()) context.drawGuiTexture(SLOT_TEXTURE, recipeSlot.x(), recipeSlot.y(), 18, 18);
                context.drawItem(recipeSlot.stack(), recipeSlot.x() + 1, recipeSlot.y() + 1);
            }

            ScreenPos arrowLocation = recipe.getArrowLocation(125, 75);
            if (arrowLocation != null) context.drawGuiTexture(ARROW_TEXTURE, arrowLocation.x(), arrowLocation.y(), 24, 16);

			recipe.render(context, 125, 75, mouseX, mouseY);

            matrices.pop();
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
            if (mouseX >= textX && mouseX <= textX + MAX_TEXT_WIDTH + 4 && mouseY >= textY && mouseY <= textY + 9) {
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

    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        for (ResultButtonWidget button : resultButtons)
            if (button.isMouseOver(mouseX, mouseY))
                button.renderTooltip(context, mouseX, mouseY);
        for (SkyblockRecipe.RecipeSlot recipeSlot : recipeSlots) {
            int shiftedMouseX = mouseX - parentX - 11;
            int shiftedMouseY = mouseY - parentY - 31 - 25;
            if (isMouseOverSlot(recipeSlot, shiftedMouseX, shiftedMouseY)) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.currentScreen == null) continue;
                List<Text> tooltip = Screen.getTooltipFromItem(client, recipeSlot.stack());
                client.currentScreen.setTooltip(tooltip.stream().map(Text::asOrderedText).toList());
            }
        }
        RenderSystem.enableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (ResultButtonWidget button : resultButtons) {
            if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                String internalName = ItemUtils.getItemId(button.itemStack);
                if (internalName.isEmpty()) {
                    continue;
                }
                fetchRecipesAndUpdate(button.itemStack);
                return true;
            }
        }
        for (SkyblockRecipe.RecipeSlot recipeSlot : recipeSlots) {
            int shiftedMouseX = (int) (mouseX - parentX - 11);
            int shiftedMouseY = (int) (mouseY - parentY - 31 - 25);
            if (isMouseOverSlot(recipeSlot, shiftedMouseX, shiftedMouseY)) {

                fetchRecipesAndUpdate(recipeSlot.stack());
                return true;
            }
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

    private static boolean isMouseOverSlot(SkyblockRecipe.RecipeSlot recipeSlot, int mouseX, int mouseY) {
        return recipeSlot.x() <= mouseX && mouseX < recipeSlot.x() + 18 && recipeSlot.y() <= mouseY && mouseY < recipeSlot.y() + 18;
    }

    private void fetchRecipesAndUpdate(ItemStack recipeSlot) {
        List<SkyblockRecipe> recipes = ItemRepository.getRecipesAndUsages(recipeSlot);
        if (!recipes.isEmpty()) {
            this.recipeResults = recipes;
            this.currentPage = 0;
            this.pageCount = recipes.size();
            this.displayRecipes = true;
            this.updateButtons();
        }
    }

    private boolean focused = false;

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

}
