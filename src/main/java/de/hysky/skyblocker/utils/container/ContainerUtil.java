package de.hysky.skyblocker.utils.container;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;

/**
 * Utility methods for working with {@link ContainerMatcher}s
 */
public final class ContainerUtil {
	private ContainerUtil() {}

	/**
	 * Removes the edge slots from the given slots map.
	 * Edge slots are defined as the first and last row, and the first and last column of each row.
	 *
	 * @param slots The map of slot IDs to ItemStacks.
	 * @param rows The number of rows in the container.
	 */
	public static void trimEdges(Int2ObjectMap<ItemStack> slots, int rows) {
		for (int i = 0; i < rows; i++) {
			slots.remove(9 * i);
			slots.remove(9 * i + 8);
		}
		for (int i = 1; i < 8; i++) {
			slots.remove(i);
			slots.remove((rows - 1) * 9 + i);
		}
	}

	/**
	 * @param slotId The slot ID to check.
	 * @param rows The number of rows in the container.
	 * @return Whether the slot is an edge slot in a container with the given number of rows.
	 */
	public static boolean isEdgeSlot(int slotId, int rows) {
		if (slotId < 0 || slotId >= rows * 9) return false;
		int row = slotId / 9;
		int col = slotId % 9;
		return col == 0 || col == 8 || row == 0 || row == rows - 1;
	}
}
