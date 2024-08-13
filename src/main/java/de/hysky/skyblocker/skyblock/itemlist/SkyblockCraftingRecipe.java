package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.utils.ItemUtils;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SkyblockCraftingRecipe {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkyblockCraftingRecipe.class);
    private final String craftText;
    private final List<ItemStack> grid = new ArrayList<>(9);
    private ItemStack result;

    public SkyblockCraftingRecipe(String craftText) {
        this.craftText = craftText;
    }

    public static SkyblockCraftingRecipe fromNEURecipe(NEUCraftingRecipe neuCraftingRecipe) {
        SkyblockCraftingRecipe recipe = new SkyblockCraftingRecipe(neuCraftingRecipe.getExtraText() != null ? neuCraftingRecipe.getExtraText() : "");
        for (NEUIngredient input : neuCraftingRecipe.getInputs()) {
            recipe.grid.add(getItemStack(input));
        }
        recipe.result = getItemStack(neuCraftingRecipe.getOutput());
        return recipe;
    }

    private static ItemStack getItemStack(NEUIngredient input) {
        if (input != NEUIngredient.SENTINEL_EMPTY) {
            ItemStack stack = ItemRepository.getItemStack(input.getItemId());
            if (stack != null) {
                return stack.copyWithCount((int) input.getAmount());
            } else {
                LOGGER.warn("[Skyblocker Recipe] Unable to find item {}", input.getItemId());
            }
        }
        return Items.AIR.getDefaultStack();
    }

    public List<ItemStack> getGrid() {
        return grid;
    }

    public ItemStack getResult() {
        return result;
    }

    public String getCraftText() {
        return craftText;
    }

    public Identifier getId() {
        return Identifier.of("skyblock", ItemUtils.getItemId(getResult()).toLowerCase().replace(';', '_') + "_" + getResult().getCount());
    }
}
