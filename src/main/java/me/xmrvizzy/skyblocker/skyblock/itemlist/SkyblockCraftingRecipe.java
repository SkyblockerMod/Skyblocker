package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SkyblockCraftingRecipe {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkyblockCraftingRecipe.class);
    String text = "";
    final List<ItemStack> grid = new ArrayList<>(9);
    ItemStack result;

    public static SkyblockCraftingRecipe fromJsonObject(JsonObject jsonObj) {
        SkyblockCraftingRecipe recipe = new SkyblockCraftingRecipe();
        if (jsonObj.has("crafttext")) recipe.text = jsonObj.get("crafttext").getAsString();
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("A1").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("A2").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("A3").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("B1").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("B2").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("B3").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("C1").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("C2").getAsString()));
        recipe.grid.add(getItemStack(jsonObj.getAsJsonObject("recipe").get("C3").getAsString()));
        recipe.result = ItemRegistry.itemsMap.get(jsonObj.get("internalname").getAsString());
        return recipe;
    }

    private static ItemStack getItemStack(String internalName) {
        try {
            if (internalName.length() > 0) {
                int count = internalName.split(":").length == 1 ? 1 : Integer.parseInt(internalName.split(":")[1]);
                internalName = internalName.split(":")[0];
                ItemStack itemStack = ItemRegistry.itemsMap.get(internalName).copy();
                itemStack.setCount(count);
                return itemStack;
            }
        } catch (Exception e) {
            LOGGER.error("[Skyblocker-Recipe] " + internalName, e);
        }
        return Items.AIR.getDefaultStack();
    }

    public List<ItemStack> getGrid() {
        return grid;
    }

    public ItemStack getResult() {
        return result;
    }
}
