package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Skyblock Crafting Recipe display class for REI
 */
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

	@Override
	public Optional<Identifier> getDisplayLocation() {
		return Optional.empty();
	}

	@Override
	public @Nullable DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}
}
