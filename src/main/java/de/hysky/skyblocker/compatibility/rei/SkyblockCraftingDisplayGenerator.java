package me.xmrvizzy.skyblocker.compatibility.rei;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import me.xmrvizzy.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkyblockCraftingDisplayGenerator implements DynamicDisplayGenerator<SkyblockCraftingDisplay> {

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack)) return Optional.empty();
        EntryStack<ItemStack> inputItem = EntryStacks.of((ItemStack) entry.getValue());
        List<SkyblockCraftingRecipe> filteredRecipes = ItemRegistry.getRecipesStream()
                .filter(recipe -> {
                    ItemStack itemStack = inputItem.getValue();
                    ItemStack itemStack1 = recipe.getResult();
                    return ItemUtils.getItemId(itemStack1).equals(ItemUtils.getItemId(itemStack));
                })
                .toList();

        return Optional.of(generateDisplays(filteredRecipes));
    }

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getUsageFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack)) return Optional.empty();
        EntryStack<ItemStack> inputItem = EntryStacks.of((ItemStack) entry.getValue());
        List<SkyblockCraftingRecipe> filteredRecipes = ItemRegistry.getRecipesStream()
                .filter(recipe -> {
                    for (ItemStack item : recipe.getGrid()) {
                        if(!ItemUtils.getItemId(item).isEmpty()) {
                            ItemStack itemStack = inputItem.getValue();
                            if (ItemUtils.getItemId(item).equals(ItemUtils.getItemId(itemStack))) return true;
                        }
                    }
                    return false;
                })
                .toList();
        return Optional.of(generateDisplays(filteredRecipes));
    }

    /**
     * Generate Displays from a list of recipes
     */
    private List<SkyblockCraftingDisplay> generateDisplays(List<SkyblockCraftingRecipe> recipes) {
        List<SkyblockCraftingDisplay> displays = new ArrayList<>();
        for (SkyblockCraftingRecipe recipe : recipes) {
            List<EntryIngredient> inputs = new ArrayList<>();
            List<EntryIngredient> outputs = new ArrayList<>();

            ArrayList<EntryStack<ItemStack>> inputEntryStacks = new ArrayList<>();
            recipe.getGrid().forEach((item) -> inputEntryStacks.add(EntryStacks.of(item)));

            for (EntryStack<ItemStack> entryStack : inputEntryStacks) {
                inputs.add(EntryIngredient.of(entryStack));
            }
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getResult())));

            displays.add(new SkyblockCraftingDisplay(inputs, outputs, recipe.getCraftText()));
        }
        return displays;
    }
}
