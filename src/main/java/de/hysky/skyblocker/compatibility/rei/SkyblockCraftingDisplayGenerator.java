package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.skyblock.hunting.Attribute;
import de.hysky.skyblocker.skyblock.hunting.Attributes;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SkyblockCraftingDisplayGenerator implements DynamicDisplayGenerator<SkyblockCraftingDisplay> {

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack)) return Optional.empty();
        EntryStack<ItemStack> inputItem = EntryStacks.of((ItemStack) entry.getValue());
        List<SkyblockCraftingRecipe> filteredRecipes = ItemRepository.getRecipesStream()
                .filter(recipe -> {
                    ItemStack itemStack = inputItem.getValue();
					ItemStack itemStack2 = recipe.getResult();
					String itemId = ItemUtils.getItemId(itemStack);
					String itemId2 = ItemUtils.getItemId(itemStack2);

					if (!itemId.equals("ATTRIBUTE_SHARD")) {
						return itemId.equals(itemId2);
					}

					// Special case for attribute shards since all have the same Item ID
					if (!itemId2.equals("ATTRIBUTE_SHARD")) return false;
					Attribute attribute = Attributes.getAttributeFromItemName(itemStack);
					Attribute attribute2 = Attributes.getAttributeFromItemName(itemStack2);
					if (attribute == null || attribute2 == null) return false;
					return attribute.name().equals(attribute2.name());
                })
                .toList();

        return Optional.of(generateDisplays(filteredRecipes));
    }

    @Override
    public Optional<List<SkyblockCraftingDisplay>> getUsageFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack)) return Optional.empty();
        EntryStack<ItemStack> inputItem = EntryStacks.of((ItemStack) entry.getValue());
        List<SkyblockCraftingRecipe> filteredRecipes = ItemRepository.getRecipesStream()
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
                if (entryStack.isEmpty()) {
                    inputs.add(EntryIngredient.empty());
                } else {
                    inputs.add(EntryIngredient.of(entryStack));
                }
            }
            outputs.add(EntryIngredient.of(EntryStacks.of(recipe.getResult())));

            displays.add(new SkyblockCraftingDisplay(inputs, outputs, recipe.getCraftText()));
        }
        return displays;
    }
}
