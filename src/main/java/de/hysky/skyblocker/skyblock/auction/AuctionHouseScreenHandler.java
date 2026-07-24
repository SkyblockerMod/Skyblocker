package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AuctionHouseScreenHandler extends ChestMenu {
	public AuctionHouseScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, Container inventory, int rows, boolean isView) {
		super(type, syncId, playerInventory, inventory, rows);

		int yOffset = (rows - 4) * 18;
		// Shift player inventory by 2 pixels and also remove the yOffset
		for (int i = rows * 9; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			SlotAccessor slotAccessor = (SlotAccessor) slot;
			slotAccessor.setY(slot.y + 2 - yOffset);
		}
		// disable ALL THE OTHER SLOTS MWAHAHAHA and also move the good ones around and stuff
		for (int i = 0; i < rows * 9; i++) {
			int lineI = i % 9;
			Slot slot = slots.get(i);
			if (!isView && i > 9 && i < (rows - 1) * 9 && lineI > 1 && lineI < 8) {
				int miniInventorySlot = lineI - 2 + (i / 9 - 1) * 6;
				SlotAccessor slotAccessor = (SlotAccessor) slot;
				slotAccessor.setX(8 + miniInventorySlot % 8 * 18);
				slotAccessor.setY(18 + miniInventorySlot / 8 * 18);
			} else {
				slots.set(i, new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y) {
					@Override
					public boolean isActive() {
						return false;
					}
				});
			}
		}
	}

	public static AuctionHouseScreenHandler of(ChestMenu original, boolean isView) {
		assert Minecraft.getInstance().player != null;
		return new AuctionHouseScreenHandler(original.getType(),
				original.containerId,
				Minecraft.getInstance().player.getInventory(),
				original.getContainer(),
				original.getRowCount(),
				isView);
	}

	@Override
	public void setItem(int slot, int revision, ItemStack stack) {
		super.setItem(slot, revision, stack);
	}
}
