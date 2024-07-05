package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackpackItemLoader extends ItemLoader {
    @Override
    public List<ItemStack> loadItems(JsonObject data) {
        List<ItemStack> backpackItems = new ArrayList<>();

        // Sort the data by keys numerically
        List<Map.Entry<String, JsonElement>> sortedEntries = data.entrySet().stream()
                .sorted((e1, e2) -> {
                    int key1 = Integer.parseInt(e1.getKey());
                    int key2 = Integer.parseInt(e2.getKey());
                    return Integer.compare(key1, key2);
                }).toList();

        for (int i = 0; i < sortedEntries.size(); i++) {
            backpackItems.addAll(super.loadItems(sortedEntries.get(i).getValue().getAsJsonObject()));
            int padding =  (i + 1) * 45 % (backpackItems.isEmpty() ? 1 : backpackItems.size());
            for (int j = 0; j < padding; j++) {
                backpackItems.add(ItemStack.EMPTY);
            }
        }

        return backpackItems;
    }
}
