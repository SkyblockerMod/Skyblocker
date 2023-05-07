package me.xmrvizzy.skyblocker.skyblock.rei;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkyblockCraftingDisplayGenerator implements DynamicDisplayGenerator<SkyblockCraftingDisplay> {

    public SkyblockCraftingDisplayGenerator() {
        for (SkyblockCraftingRecipe recipe : ItemRegistry.getRecipes()) {
            List<EntryIngredient> inputs = new ArrayList<>();
            List<EntryIngredient> outputs = new ArrayList<>();

            ArrayList<EntryStack<ItemStack>> inputEntryStacks = new ArrayList<>();
            recipe.getGrid().forEach((item) -> inputEntryStacks.add(EntryStacks.of(item)));

            for (EntryStack<ItemStack> entryStack : inputEntryStacks) {
                inputs.add(EntryIngredient.of(entryStack));
            }
//            inputs.add(EntryIngredient.of(inputEntryStacks.get(0)));
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getResult())));
//            displayRegistry.add(new SkyblockCraftingDisplay(null, inputs, outputs));
        }
    }

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack)) return Optional.empty();
        EntryStack<ItemStack> itemStackEntryStack = EntryStacks.of((ItemStack) entry.getValue());
        ItemRegistry.getRecipes().stream().filter(recipe -> ItemRegistry.getInternalName(recipe.getResult()).equals(ItemRegistry.getInternalName(itemStackEntryStack.getValue())));
        return null;
    }

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getUsageFor(EntryStack<?> entry) {
        return null;
    }

    @Override
    public Optional<List<SkyblockCraftingDisplay>> generate(ViewSearchBuilder builder) {
        return null;
    }
}
