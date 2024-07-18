package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.Identifier;

import java.util.List;

public class SkyblockRecipeDisplay implements Display {

    private final Identifier category;
    private final SkyblockRecipe recipe;

    public SkyblockRecipeDisplay(SkyblockRecipe recipe) {
        this.category = recipe.getCategoryIdentifier();
        this.recipe = recipe;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return recipe.getInputs().stream().map(EntryStacks::of).map(EntryIngredient::of).toList();
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return recipe.getOutputs().stream().map(EntryStacks::of).map(EntryIngredient::of).toList();
    }

    public SkyblockRecipe getRecipe() {
        return recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CategoryIdentifier.of(category);
    }
}
