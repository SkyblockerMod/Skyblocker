package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Utils;
import java.util.Arrays;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class SkyblockCraftingTableScreenHandler extends ChestMenu {

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

	public SkyblockCraftingTableScreenHandler(MenuType<?> type, int syncId, Inventory playerInventory, Container inventory, int rows) {
		super(type, syncId, playerInventory, inventory, rows);
		mirrorverse = Utils.getArea() == Area.MIRRORVERSE;
		int[] activeSlots = mirrorverse ? riftNormalSlots : normalSlots;

		for (int i = 0; i < rows * 9; i++) {
			Slot originalSlot = slots.get(i);
			if (Arrays.binarySearch(activeSlots, i) >= 0) {
				int[] coords = getCoords(i);
				Slot slot = new Slot(originalSlot.container, originalSlot.getContainerSlot(), coords[0], coords[1]);
				slot.index = i;
				slots.set(i, slot);
			} else {
				DisabledSlot slot = new DisabledSlot(originalSlot.container, originalSlot.getContainerSlot(), -20, -20);
				slot.index = i;
				slots.set(i, slot);
			}
		}
		int yOffset = (rows - 4) * 18 + 19;
		for (int i = rows * 9; i < slots.size(); i++) {
			Slot originalSlot = slots.get(i);
			Slot slot = new Slot(originalSlot.container, originalSlot.getContainerSlot(), originalSlot.x, originalSlot.y - yOffset);
			slot.index = i;
			slots.set(i, slot);
		}
	}

	public SkyblockCraftingTableScreenHandler(ChestMenu handler, Inventory playerInventory) {
		this(handler.getType(), handler.containerId, playerInventory, handler.getContainer(), handler.getRowCount());
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
				return new int[]{152, y};
			}
			int gridX = slot % 9 - 1;
			int gridY = slot / 9 - 1;
			return new int[]{30 + gridX * 18, 17 + gridY * 18};
		}
	}

	public static class DisabledSlot extends Slot {

		public DisabledSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean isActive() {
			return false;
		}
	}
}
