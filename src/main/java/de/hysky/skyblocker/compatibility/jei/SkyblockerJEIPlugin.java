package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@JeiPlugin
public class SkyblockerJEIPlugin implements IModPlugin {
    private SkyblockCraftingRecipeCategory skyblockCraftingRecipeCategory;

    @Override
    @NotNull
    public Identifier getPluginUid() {
        return new Identifier(SkyblockerMod.NAMESPACE, "skyblock");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(ItemRepository.getItemsStream().map(ItemStack::getItem).toArray(Item[]::new));
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        skyblockCraftingRecipeCategory = new SkyblockCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper());
        registration.addRecipeCategories(skyblockCraftingRecipeCategory);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(skyblockCraftingRecipeCategory.getRecipeType(), ItemRepository.getRecipesStream().map(recipe ->
                new RecipeEntry<CraftingRecipe>(recipe.getId(), new ShapedRecipe("", CraftingRecipeCategory.MISC, RawShapedRecipe.create(Map.of(
                        'a', Ingredient.ofStacks(recipe.getGrid().get(0)),
                        'b', Ingredient.ofStacks(recipe.getGrid().get(1)),
                        'c', Ingredient.ofStacks(recipe.getGrid().get(2)),
                        'd', Ingredient.ofStacks(recipe.getGrid().get(3)),
                        'e', Ingredient.ofStacks(recipe.getGrid().get(4)),
                        'f', Ingredient.ofStacks(recipe.getGrid().get(5)),
                        'g', Ingredient.ofStacks(recipe.getGrid().get(6)),
                        'h', Ingredient.ofStacks(recipe.getGrid().get(7)),
                        'i', Ingredient.ofStacks(recipe.getGrid().get(8))
                ), "abc", "def", "ghi"), recipe.getResult()))
        ).toList());
    }
}
