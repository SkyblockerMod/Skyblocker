package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ItemRegistry {
    protected static final String REMOTE_ITEM_REPO = "https://github.com/KonaeAkira/NotEnoughUpdates-REPO.git";
    protected static final Path LOCAL_ITEM_REPO_DIR = FabricLoader.getInstance().getConfigDir().resolve("skyblocker/item-repo");

    private static final Path ITEM_LIST_DIR = LOCAL_ITEM_REPO_DIR.resolve("items");

    protected static final List<ItemStack> items = new ArrayList<>();
    protected static final Map<String, ItemStack> itemsMap = new HashMap<>();
    protected static final List<SkyblockCraftingRecipe> recipes = new ArrayList<>();

    // TODO: make async
    public static void init() {
        updateItemRepo();
        ItemStackBuilder.init();
        importItemFiles();
    }

    private static void updateItemRepo() {
        if (!Files.isDirectory(LOCAL_ITEM_REPO_DIR)) {
            try {
                Git.cloneRepository()
                        .setURI(REMOTE_ITEM_REPO)
                        .setDirectory(LOCAL_ITEM_REPO_DIR.toFile())
                        .setBranchesToClone(List.of("refs/heads/master"))
                        .setBranch("refs/heads/master")
                        .call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Git.open(LOCAL_ITEM_REPO_DIR.toFile()).pull().call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void importItemFiles() {
        List<JsonObject> jsonObjs = new ArrayList<>();

        File dir = ITEM_LIST_DIR.toFile();
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            Path path = ITEM_LIST_DIR.resolve(file.getName());
            try {
                String fileContent = Files.readString(path);
                jsonObjs.add(JsonParser.parseString(fileContent).getAsJsonObject());
            } catch (Exception e) {
                e.printStackTrace();
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
                else
                    return lhsInternalName.compareTo(rhsInternalName);
            }
            return lhsFamilyName.compareTo(rhsFamilyName);
        });
    }

    public static List<SkyblockCraftingRecipe> getRecipes(String internalName) {
        List<SkyblockCraftingRecipe> result = new ArrayList<>();
        for (SkyblockCraftingRecipe recipe : recipes)
            if (getInternalName(recipe.result).equals(internalName))
                result.add(recipe);
        for (SkyblockCraftingRecipe recipe : recipes)
            for (ItemStack ingredient : recipe.grid)
                if (!ingredient.getItem().equals(Items.AIR) && getInternalName(ingredient).equals(internalName)) {
                    result.add(recipe);
                    break;
                }
        return result;
    }

    public static List<SkyblockCraftingRecipe> getRecipes() {
        return recipes;
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
}

