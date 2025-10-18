package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WardrobeInventoryItemLoader extends ItemLoader {
    private final int activeSlot;
    private final JsonObject activeArmorSet;

    public WardrobeInventoryItemLoader(JsonObject inventory) {
        this.activeSlot = inventory.get("wardrobe_equipped_slot").getAsInt();
        this.activeArmorSet = inventory.get("inv_armor").getAsJsonObject();
    }

    @Override
    public List<ItemStack> loadItems(JsonObject data) {
        List<ItemStack> itemList = new ArrayList<>();

        try {
            itemList.addAll(super.loadItems(data));
            if (activeSlot != -1) {
                List<ItemStack> activeArmour = super.loadItems(activeArmorSet).reversed();
                for (int i = 0; i < 4; i++) {
                    int baseIndex = (activeSlot - 1) % 9;
                    int page = (activeSlot - 1) / 9;
                    int slotIndex = (page * 36) + (i * 9) + baseIndex;
                    itemList.set(slotIndex, activeArmour.get(i));
                }
            }
        } catch (Exception e) {
            ProfileViewerScreen.LOGGER.error("[Skyblocker Profile Viewer] Failed to load wardrobe items", e);
        }

        return itemList;
    }
}
