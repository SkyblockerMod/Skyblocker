package de.hysky.skyblocker.compatibility.emi;

import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SkyblockEmiRecipe extends EmiCraftingRecipe {
    private final String craftText;

    public SkyblockEmiRecipe(SkyblockCraftingRecipe recipe) {
        super(recipe.getGrid().stream().map(EmiStack::of).map(EmiIngredient.class::cast).toList(), EmiStack.of(recipe.getResult()), recipe.getId());
        this.craftText = recipe.getCraftText();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return SkyblockerEMIPlugin.SKYBLOCK;
    }

    @Override
    public int getDisplayHeight() {
        return super.getDisplayHeight() + (craftText.isEmpty() ? 0 : 10);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        widgets.addText(Text.of(craftText), 59 - MinecraftClient.getInstance().textRenderer.getWidth(craftText) / 2, 55, 0xFFFFFF, true);
    }
}
