package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ItemRegistry {
    protected static final String REMOTE_ITEM_REPO_DIR = "https://github.com/KonaeAkira/NotEnoughUpdates-REPO.git";
    protected static final String LOCAL_ITEM_REPO_DIR = "./config/skyblocker/items-repo/";

    private static final String ITEM_LIST_DIR = LOCAL_ITEM_REPO_DIR + "items/";

    protected static List<ItemStack> items = new ArrayList<>();
    protected static Map<String, ItemStack> itemsMap = new HashMap<>();
    protected static List<Recipe> recipes = new ArrayList<>();

    // TODO: make async
    public static void init() {
        updateItemRepo();
        ItemStackBuilder.init();
        importItemFiles();
    }

    private static void updateItemRepo() {
        if (!Files.isDirectory(Paths.get(LOCAL_ITEM_REPO_DIR))) {
            try {
                Git.cloneRepository()
                        .setURI(REMOTE_ITEM_REPO_DIR)
                        .setDirectory(new File(LOCAL_ITEM_REPO_DIR))
                        .setBranchesToClone(List.of("refs/heads/master"))
                        .setBranch("refs/heads/master")
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Git.open(new File(LOCAL_ITEM_REPO_DIR)).pull().call();
            } catch (GitAPIException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void importItemFiles() {
        List<JsonObject> jsonObjs = new ArrayList<>();

        File dir = new File(ITEM_LIST_DIR);
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            String path = ITEM_LIST_DIR + "/" + file.getName();
            try {
                String fileContent = Files.readString(Paths.get(path));
                jsonObjs.add(JsonParser.parseString(fileContent).getAsJsonObject());
            } catch (IOException e) {
                System.err.println("Couldn't import " + path);
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
                recipes.add(Recipe.fromJsonObject(jsonObj));
            }

        items.sort((lhs, rhs) -> {
            String lhsInternalName = lhs.getNbt().getCompound("ExtraAttributes").getString("id");
            String lhsFamilyName = lhsInternalName.replaceAll(".\\d+$", "");
            String rhsInternalName = rhs.getNbt().getCompound("ExtraAttributes").getString("id");
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

    public static List<Recipe> getRecipes(String internalName) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe recipe : recipes)
            if (recipe.result.getNbt().getCompound("ExtraAttributes").getString("id").equals(internalName))
                result.add(recipe);
        for (Recipe recipe : recipes)
            for (ItemStack ingredient : recipe.grid)
                if (!ingredient.getItem().equals(Items.AIR) && ingredient.getNbt().getCompound("ExtraAttributes").getString("id").equals(internalName)) {
                    result.add(recipe);
                    break;
                }
        return result;
    }
}

class Recipe {
    String text = "";
    List<ItemStack> grid = new ArrayList<>(9);
    ItemStack result;

    public static Recipe fromJsonObject(JsonObject jsonObj) {
        Recipe recipe = new Recipe();
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
        if (internalName.length() > 0) {
            int count = Integer.parseInt(internalName.split(":")[1]);
            internalName = internalName.split(":")[0];
            ItemStack itemStack = ItemRegistry.itemsMap.get(internalName).copy();
            itemStack.setCount(count);
            return itemStack;
        }
        return Items.AIR.getDefaultStack();
    }
}