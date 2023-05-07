package me.xmrvizzy.skyblocker.skyblock.rei;


import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.recipe.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Skyblock Crafting Recipe display class for REI
 */
public class SkyblockCraftingDisplay extends DefaultCustomDisplay {
    public SkyblockCraftingDisplay(@Nullable Recipe<?> possibleRecipe, List<EntryIngredient> input, List<EntryIngredient> output) {
        super(possibleRecipe, input, output);
    }
}
