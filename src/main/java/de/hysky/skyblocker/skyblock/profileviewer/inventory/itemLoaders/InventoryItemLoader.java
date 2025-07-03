package de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemLoader extends ItemLoader {

    private static final String[] INVENTORIES = {"inv_contents", "inv_armor", "equipment_contents"};

    @Override
    public List<ItemStack> loadItems(JsonObject data) {
        List<ItemStack> inventoryItems = new ArrayList<>();
        for (String inventory : INVENTORIES) {
            List<ItemStack> inv = super.loadItems(data.getAsJsonObject(inventory));
            switch (inventory) {
                case "inv_armor" -> inventoryItems.addAll(inv.reversed());
                case "inv_contents" -> {
                        inventoryItems.addAll(inv.subList(9, inv.size()));
                        inventoryItems.addAll(inv.subList(0, 9));
                    }
                default -> inventoryItems.addAll(inv);
            }
        }
        return inventoryItems;
    }
}
