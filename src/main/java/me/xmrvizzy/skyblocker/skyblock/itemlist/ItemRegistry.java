package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.xmrvizzy.skyblocker.utils.NEURepo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ItemRegistry {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemRegistry.class);
    protected static final Path ITEM_LIST_DIR = NEURepo.LOCAL_REPO_DIR.resolve("items");

    protected static final List<ItemStack> items = new ArrayList<>();
    protected static final Map<String, ItemStack> itemsMap = new HashMap<>();
    protected static final List<SkyblockCraftingRecipe> recipes = new ArrayList<>();
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static boolean filesImported = false;

    public static void init() {
        NEURepo.runAsyncAfterLoad(ItemStackBuilder::loadPetNums);
        NEURepo.runAsyncAfterLoad(ItemRegistry::importItemFiles);
    }

    private static void importItemFiles() {
        List<JsonObject> jsonObjs = new ArrayList<>();

        File dir = ITEM_LIST_DIR.toFile();
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            Path path = ITEM_LIST_DIR.resolve(file.getName());
            try {
                String fileContent = Files.readString(path);
                jsonObjs.add(JsonParser.parseString(fileContent).getAsJsonObject());
            } catch (Exception e) {
                LOGGER.error("Failed to read file " + path, e);
            }
        }

        for (JsonObject jsonObj : jsonObjs) {
            String internalName = jsonObj.get("internalname").getAsString();
            ItemStack itemStack = ItemStackBuilder.parseJsonObj(jsonObj);
            items.add(itemStack);
            itemsMap.put(internalName, itemStack);
        }
        for (JsonObject jsonObj : jsonObjs)
            if (jsonObj.has("recipe")) {
                recipes.add(SkyblockCraftingRecipe.fromJsonObject(jsonObj));
            }

        items.sort((lhs, rhs) -> {
            String lhsInternalName = getInternalName(lhs);
            String lhsFamilyName = lhsInternalName.replaceAll(".\\d+$", "");
            String rhsInternalName = getInternalName(rhs);
            String rhsFamilyName = rhsInternalName.replaceAll(".\\d+$", "");
            if (lhsFamilyName.equals(rhsFamilyName)) {
                if (lhsInternalName.length() != rhsInternalName.length())
                    return lhsInternalName.length() - rhsInternalName.length();
                else return lhsInternalName.compareTo(rhsInternalName);
            }
            return lhsFamilyName.compareTo(rhsFamilyName);
        });
        filesImported = true;
    }

    public static String getWikiLink(String internalName) {
        try {
            String fileContent = Files.readString(ITEM_LIST_DIR.resolve(internalName + ".json"));
            JsonObject fileJson = JsonParser.parseString(fileContent).getAsJsonObject();
            //TODO optional official or unofficial wiki link
            try {
                return fileJson.get("info").getAsJsonArray().get(1).getAsString();
            } catch (IndexOutOfBoundsException e) {
                return fileJson.get("info").getAsJsonArray().get(0).getAsString();
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.error("Failed to read item file " + internalName + ".json", e);
            if (client.player != null) {
                client.player.sendMessage(Text.of("Can't locate a wiki article for this item..."), false);
            }
            return null;
        }
    }

    public static List<SkyblockCraftingRecipe> getRecipes(String internalName) {
        List<SkyblockCraftingRecipe> result = new ArrayList<>();
        for (SkyblockCraftingRecipe recipe : recipes)
            if (getInternalName(recipe.result).equals(internalName)) result.add(recipe);
        for (SkyblockCraftingRecipe recipe : recipes)
            for (ItemStack ingredient : recipe.grid)
                if (!ingredient.getItem().equals(Items.AIR) && getInternalName(ingredient).equals(internalName)) {
                    result.add(recipe);
                    break;
                }
        return result;
    }

    public static Stream<SkyblockCraftingRecipe> getRecipesStream() {
        return recipes.stream();
    }

    public static Stream<ItemStack> getItemsStream() {
        return items.stream();
    }

    /**
     * Get Internal name of an ItemStack
     *
     * @param itemStack ItemStack to get internal name from
     * @return internal name of the given ItemStack
     */
    public static String getInternalName(ItemStack itemStack) {
        if (itemStack.getNbt() == null) return "";
        return itemStack.getNbt().getCompound("ExtraAttributes").getString("id");
    }

    public static ItemStack getItemStack(String internalName) {
        return itemsMap.get(internalName);
    }
}

