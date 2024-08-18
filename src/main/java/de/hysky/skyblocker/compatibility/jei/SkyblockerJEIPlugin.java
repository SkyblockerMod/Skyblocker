package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
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
        return Identifier.of(SkyblockerMod.NAMESPACE, "skyblock");
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        SubtypeInterpreters interpreters = ((SubtypeRegistration) registration).getInterpreters();
        ItemRepository.getItemsStream().filter(stack -> !interpreters.contains(VanillaTypes.ITEM_STACK, stack)).map(ItemStack::getItem).distinct().forEach(item ->
                registration.registerSubtypeInterpreter(item, (stack, context) -> ItemStackComponentizationFixer.componentsAsString(stack))
        );
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        skyblockCraftingRecipeCategory = new SkyblockCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper());
        skyblockCraftingRecipeCategory.addExtension(CraftingRecipe.class, new CraftingCategoryExtension());
        registration.addRecipeCategories(skyblockCraftingRecipeCategory);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ItemRepository.getItems());
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
