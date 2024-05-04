package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.NEURecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ItemRepository {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemRepository.class);

    private static final List<ItemStack> items = new ArrayList<>();
    private static final Map<String, ItemStack> itemsMap = new HashMap<>();
    private static final List<SkyblockCraftingRecipe> recipes = new ArrayList<>();
    private static boolean filesImported = false;

    public static void init() {
        NEURepoManager.runAsyncAfterLoad(ItemStackBuilder::loadPetNums);
        NEURepoManager.runAsyncAfterLoad(ItemRepository::importItemFiles);
    }

    private static void importItemFiles() {
        NEURepoManager.NEU_REPO.getItems().getItems().values().forEach(ItemRepository::loadItem);
        NEURepoManager.NEU_REPO.getItems().getItems().values().forEach(ItemRepository::loadRecipes);

        items.sort((lhs, rhs) -> {
            String lhsInternalName = ItemUtils.getItemId(lhs);
            String lhsFamilyName = lhsInternalName.replaceAll(".\\d+$", "");
            String rhsInternalName = ItemUtils.getItemId(rhs);
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

    private static void loadItem(NEUItem item) {
        ItemStack stack = ItemStackBuilder.fromNEUItem(item);
        items.add(stack);
        itemsMap.put(item.getSkyblockItemId(), stack);
    }

    private static void loadRecipes(NEUItem item) {
        for (NEURecipe recipe : item.getRecipes()) {
            if (recipe instanceof NEUCraftingRecipe neuCraftingRecipe) {
                recipes.add(SkyblockCraftingRecipe.fromNEURecipe(neuCraftingRecipe));
            }
        }
    }

    public static String getWikiLink(String internalName) {
        NEUItem item = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(internalName);
        if (item == null || item.getInfo() == null || item.getInfo().isEmpty()) {
            return null;
        }

        List<String> info = item.getInfo();
        String wikiLink0 = info.get(0);
        String wikiLink1 = info.size() > 1 ? info.get(1) : "";
        String wikiDomain = SkyblockerConfigManager.get().general.wikiLookup.officialWiki ? "https://wiki.hypixel.net" : "https://hypixel-skyblock.fandom.com";
        if (wikiLink0.startsWith(wikiDomain)) {
            return wikiLink0;
        } else if (wikiLink1.startsWith(wikiDomain)) {
            return wikiLink1;
        }
        return null;
    }

    public static List<SkyblockCraftingRecipe> getRecipes(String internalName) {
        List<SkyblockCraftingRecipe> result = new ArrayList<>();
        for (SkyblockCraftingRecipe recipe : recipes) {
            if (ItemUtils.getItemId(recipe.getResult()).equals(internalName)) result.add(recipe);
        }
        for (SkyblockCraftingRecipe recipe : recipes) {
            for (ItemStack ingredient : recipe.getGrid()) {
                if (!ingredient.getItem().equals(Items.AIR) && ItemUtils.getItemId(ingredient).equals(internalName)) {
                    result.add(recipe);
                    break;
                }
            }
        }
        return result;
    }

    public static boolean filesImported() {
        return filesImported;
    }

    public static void setFilesImported(boolean filesImported) {
        ItemRepository.filesImported = filesImported;
    }

    public static List<ItemStack> getItems() {
        return items;
    }

    public static Stream<ItemStack> getItemsStream() {
        return items.stream();
    }

    @Nullable
    public static ItemStack getItemStack(String internalName) {
        return itemsMap.get(internalName);
    }

    public static Stream<SkyblockCraftingRecipe> getRecipesStream() {
        return recipes.stream();
    }
}

