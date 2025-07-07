package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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
            int paddingNeeded = (45 - (backpackItems.size() % 45)) % 45;
            for (int j = 0; j < paddingNeeded; j++) {
                ItemStack paddingItem = Ico.GRAY_DYE.copy();
                paddingItem.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("skyblocker.profileviewer.inventory.inactive"));
                paddingItem.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("skyblocker.profileviewer.inventory.inactive.description.backpack"), Text.translatable("skyblocker.profileviewer.inventory.inactive.description.general"))));
                backpackItems.add(paddingItem);
            }
        }

        return backpackItems;
    }
}
