package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.xmrvizzy.skyblocker.skyblock.api.RepositoryUpdate;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ItemRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRegistry.class);
    protected static final String REMOTE_ITEM_REPO = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO";
    public static final Path LOCAL_ITEM_REPO_DIR = FabricLoader.getInstance().getConfigDir().resolve("skyblocker/item-repo");

    protected static final Path ITEM_LIST_DIR = LOCAL_ITEM_REPO_DIR.resolve("items");

    protected static final List<ItemStack> items = new ArrayList<>();
    protected static final Map<String, ItemStack> itemsMap = new HashMap<>();
    protected static final List<SkyblockCraftingRecipe> recipes = new ArrayList<>();
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static boolean filesImported = false;

    public static void init() {
        CompletableFuture.runAsync(ItemRegistry::updateItemRepo)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        ItemStackBuilder.init();
                        importItemFiles();
                    }
                    else {
                        LOGGER.error("[Skyblocker-ItemRegistry] " + ex);
                    }
                });
    }

    private static void updateItemRepo() {
        Git git;
        if (!Files.isDirectory(LOCAL_ITEM_REPO_DIR)) {
            try {
                git = Git.cloneRepository()
                        .setURI(REMOTE_ITEM_REPO)
                        .setDirectory(LOCAL_ITEM_REPO_DIR.toFile())
                        .setBranchesToClone(List.of("refs/heads/master"))
                        .setBranch("refs/heads/master")
                        .call();
                git.close();
                LOGGER.info("[Skyblocker Repository Update] Repository updated.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                git = Git.open(LOCAL_ITEM_REPO_DIR.toFile());
                PullResult pull = git.pull().setRebase(true).call();
                git.close();

                if (pull.getRebaseResult() == null) {
                    LOGGER.info("[Skyblocker Repository Update] No update result");
                } else if (pull.getRebaseResult().getStatus().isSuccessful()) {
                    LOGGER.info("[Skyblocker Repository Update] Status: " + pull.getRebaseResult().getStatus().name());
                } else if (!pull.getRebaseResult().getStatus().isSuccessful()) {
                    LOGGER.warn("[Skyblocker Repository Update] Status: " + pull.getRebaseResult().getStatus().name());
                }
            } catch (RepositoryNotFoundException e) {
                RepositoryUpdate.updateRepository();
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
            e.printStackTrace();
            client.player.sendMessage(Text.of("Can't locate a wiki article for this item..."), false);
            return null;
        }
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

