package de.hysky.skyblocker.compatibility.emi;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SkyblockEmiRecipe extends EmiCraftingRecipe {
    private final String craftText;

    public SkyblockEmiRecipe(SkyblockCraftingRecipe recipe) {
        super(recipe.getGrid().stream().map(EmiStack::of).map(EmiIngredient.class::cast).toList(), EmiStack.of(recipe.getResult()).comparison(Comparison.compareNbt()), Identifier.of("skyblock", ItemUtils.getItemId(recipe.getResult()).toLowerCase().replace(';', '_')));
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
