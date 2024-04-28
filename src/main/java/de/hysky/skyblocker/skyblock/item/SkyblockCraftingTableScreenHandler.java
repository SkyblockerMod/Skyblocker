package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

import java.util.Arrays;

public class SkyblockCraftingTableScreenHandler extends GenericContainerScreenHandler {

    private static final int[] normalSlots = new int[]{
            10, 11, 12,     16,
            19, 20, 21, 23, 25,
            28, 29, 30,     34
    };

    private static final int[] riftNormalSlots = new int[]{
            11, 12, 13,
            20, 21, 22, 24,
            29, 30, 31
    };

    public final boolean mirrorverse;

    public SkyblockCraftingTableScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
        super(type, syncId, playerInventory, inventory, rows);
        mirrorverse = Utils.getIslandArea().toLowerCase().contains("mirrorverse");
        int[] activeSlots = mirrorverse ? riftNormalSlots: normalSlots;

        for (int i = 0; i < rows * 9; i++) {
            Slot originalSlot = slots.get(i);
            if (Arrays.binarySearch(activeSlots, i) >= 0) {
                int[] coords = getCoords(i);
                Slot slot = new Slot(originalSlot.inventory, originalSlot.getIndex(), coords[0], coords[1]);
                slot.id = i;
                slots.set(i, slot);
            } else {
                DisabledSlot slot = new DisabledSlot(originalSlot.inventory, originalSlot.getIndex(), originalSlot.x, originalSlot.y);
                slot.id = i;
                slots.set(i, slot);
            }
        }
        int yOffset = (rows - 4) * 18 + 19;
        for (int i = rows * 9; i < slots.size(); i++) {
            Slot originalSlot = slots.get(i);
            Slot slot = new Slot(originalSlot.inventory, originalSlot.getIndex(), originalSlot.x, originalSlot.y - yOffset);
            slot.id = i;
            slots.set(i, slot);
        }
    }

    public SkyblockCraftingTableScreenHandler(GenericContainerScreenHandler handler, PlayerInventory playerInventory) {
        this(handler.getType(), handler.syncId, playerInventory, handler.getInventory(), handler.getRows());
    }

    private int[] getCoords(int slot) {
        if (mirrorverse) {
            if (slot == 24) return new int[]{124, 35};
            int gridX = slot % 9 - 2;
            int gridY = slot / 9 - 1;
            return new int[]{30 + gridX * 18, 17 + gridY * 18};
        } else {
            if (slot == 23) return new int[]{124, 35};
            if (slot == 16 || slot == 25 || slot == 34) {
                int y = (slot / 9 - 1) * 18 + 8;
                return new int[]{174, y};
            }
            int gridX = slot % 9 - 1;
            int gridY = slot / 9 - 1;
            return new int[]{30 + gridX * 18, 17 + gridY * 18};
        }
    }

    public static class DisabledSlot extends Slot {

        public DisabledSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
