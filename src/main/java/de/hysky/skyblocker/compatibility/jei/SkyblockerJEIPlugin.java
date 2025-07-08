package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.load.registration.SubtypeRegistration;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

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
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(InventoryScreen.class, new InventoryContainerHandler());
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
    	//FIXME no clue what to replace any of this with, we can't use items as that does not work
        /*registration.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ItemRepository.getItems());
        registration.addRecipes(skyblockCraftingRecipeCategory.getRecipeType(), ItemRepository.getRecipesStream().filter(skyblockRecipe -> skyblockRecipe instanceof SkyblockCraftingRecipe).map(SkyblockCraftingRecipe.class::cast).map(recipe ->
                new RecipeEntry<CraftingRecipe>(recipe.getRecipeIdentifier(), new ShapedRecipe("", CraftingRecipeCategory.MISC, RawShapedRecipe.create(Map.of(
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
        ).toList());*/
    }

    private static class InventoryContainerHandler implements IGuiContainerHandler<InventoryScreen> {
        @Override
        public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull InventoryScreen containerScreen) {
            if (!SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget || !Utils.getLocation().equals(Location.GARDEN)) return List.of();
            HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
            return Collections.singletonList(new Rect2i(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), 104, 127));
        }
    }
}
