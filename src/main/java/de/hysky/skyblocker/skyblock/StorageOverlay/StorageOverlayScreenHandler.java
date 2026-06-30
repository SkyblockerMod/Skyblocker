package de.hysky.skyblocker.skyblock.StorageOverlay;

import de.hysky.skyblocker.mixins.accessors.SlotAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StorageOverlayScreenHandler extends ChestMenu {
	private final int rows;
	public StorageOverlayScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, Container inventory, int rows, boolean isBackpack, int height) {
		super(type, syncId, playerInventory, inventory, rows);
		this.rows = rows;
		int yOffset = rows * 18;
		int shift = height - 113;
		// Shift to bottom of available space
		for (int i = rows * 9; i < slots.size(); i++) {
			Slot originalSlot = slots.get(i);
			Slot slot = new Slot(originalSlot.container, originalSlot.getContainerSlot(), originalSlot.x, originalSlot.y - yOffset + shift);
			slot.index = i;
			slots.set(i, slot);

		}

//		// disable all slots on storage

		if (!isBackpack) {
			for (int i = 0; i < rows * 9; i++) {
				Slot slot = slots.get(i);
				slots.set(i, new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y) {
					@Override
					public boolean isActive() {
						return false;
					}
				});
			}
		}
		// if backpack / echest then only disable unneeded top row
		else {
			for (int i = 0; i < 9; i++) {
				Slot slot = slots.get(i);
				slots.set(i, new Slot(slot.container, slot.getContainerSlot(), slot.x, slot.y) {
					@Override
					public boolean isActive() {
						return false;
					}
				});
			}
			//disable slots when not in menu and set to of screen until moved
			for (int i = 9; i < rows * 9; ++i) {
				Slot originalSlot = slots.get(i);
				Slot slot = new Slot(originalSlot.container, originalSlot.getContainerSlot(), 0, -20) {
					@Override
					public boolean isActive() {
						return this.y > 8 && this.y < height - 94;
					}

					@Override
					public boolean isHighlightable() {
						return this.y > 8 && this.y < height - 94;
					}
				};
				slot.index = i;
				slots.set(i, slot);
			}

		}
	}

	public StorageOverlayScreenHandler (ChestMenu handler, boolean isBackpack, int height, Inventory playerInventory) {
		this(handler.getType(), handler.containerId, playerInventory, handler.getContainer(), handler.getRowCount(),isBackpack, height);
	}

	@Override
	public void setItem(int slot, int revision, ItemStack stack) {
		super.setItem(slot, revision, stack);
	}

	public void moveBackpackSlots(int x, int y, int columns) {
		for (int i = 9; i < rows * 9; ++i) {
			int itemX = x + (i - 9) % columns * 18;
			int itemY = y + (i - 9) / columns * 18;
			Slot slot = slots.get(i);
			SlotAccessor slotAccessor = (SlotAccessor) slot;
			slotAccessor.setX(itemX);
			slotAccessor.setY(itemY);
		}
	}


}
