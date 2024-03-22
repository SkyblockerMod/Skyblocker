package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.mixin.accessor.SlotAccessor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class AuctionHouseScreenHandler extends GenericContainerScreenHandler {
    public AuctionHouseScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows, boolean isView) {
        super(type, syncId, playerInventory, inventory, rows);

        int yOffset = (rows - 4) * 18;
        // Shift player inventory by 2 pixels and also remove the yOffset
        for (int i = rows*9; i < slots.size(); i++) {
            SlotAccessor slotAccessor = (SlotAccessor) slots.get(i);
            slotAccessor.setY(2-yOffset);
        }

        if (isView) return;
        // disable ALL THE OTHER SLOTS MWAHAHAHA and also move the good ones around and stuff
        for (int i = 9; i < (rows-1)*9; i++) {
            int lineI = i % 9;
            Slot slot = slots.get(i);
            if (lineI > 1 && lineI < 8) {
                SlotAccessor slotAccessor = (SlotAccessor) slot;
                slotAccessor.setX(8+ lineI * 18);
                slotAccessor.setY(18 + (i/9 -1) * 18);
            } else {
                slots.set(i, new Slot(slot.inventory, slot.getIndex(), slot.x, slot.y){
                    @Override
                    public boolean isEnabled() {
                        return false;
                    }
                });
            }
        }
    }
}
