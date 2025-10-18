package de.hysky.skyblocker.compatibility.emi;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkyblockEmiRecipe implements EmiRecipe {
    private final Text craftText;
    private final SkyblockRecipe recipe;

    public SkyblockEmiRecipe(SkyblockRecipe recipe) {
        this.craftText = recipe.getExtraText();
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SkyblockerEMIPlugin.IDENTIFIER_CATEGORY_MAP.get(recipe.getCategoryIdentifier());
    }

    @Override
    public @Nullable Identifier getId() {
        return recipe.getRecipeIdentifier();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return recipe.getInputs().stream().map(EmiStack::of).map(EmiIngredient.class::cast).toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return recipe.getOutputs().stream().map(EmiStack::of).toList();
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 54 + (craftText.getString().isEmpty() ? 0 : 10);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        ScreenPos arrowLocation = recipe.getArrowLocation(getDisplayWidth(), getDisplayHeight());
        if (arrowLocation != null) widgets.addTexture(EmiTexture.EMPTY_ARROW, arrowLocation.x(), arrowLocation.y());
        widgets.addText(craftText, 59 - MinecraftClient.getInstance().textRenderer.getWidth(craftText) / 2, 55, 0xFFFFFF, true);
        for (SkyblockRecipe.RecipeSlot inputSlot : recipe.getInputSlots(getDisplayWidth(), getDisplayHeight())) {
            widgets.addSlot(EmiStack.of(inputSlot.stack()), inputSlot.x(), inputSlot.y());
        }
        for (SkyblockRecipe.RecipeSlot outputSlot : recipe.getOutputSlots(getDisplayWidth(), getDisplayHeight())) {
            widgets.addSlot(EmiStack.of(outputSlot.stack()), outputSlot.x(), outputSlot.y()).recipeContext(this);
        }
		widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (draw, mouseX, mouseY, delta) -> recipe.render(draw, getDisplayWidth(), getDisplayHeight(), mouseX, mouseY));
    }
}
