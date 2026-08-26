package de.hysky.skyblocker.skyblock.dungeon.terminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;

public final class OrderTerminal extends SimpleContainerSolver implements TerminalSolver {
	private static final int PANES_NUM = 14;
	private int @Nullable [] orderedSlots;
	private int currentNum = Integer.MAX_VALUE;

	public OrderTerminal() {
		super("^Click in order!$");
	}

	@Override
	public boolean isEnabled() {
		orderedSlots = null;
		currentNum = 0;
		return SkyblockerConfigManager.get().dungeons.terminals.solveOrder;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		if (orderedSlots == null && !orderSlots(slots))
			return Collections.emptyList();
		while (currentNum < PANES_NUM && slots.containsKey(orderedSlots[currentNum]) && slots.get(orderedSlots[currentNum]).is(Items.STAINED_GLASS_PANE.lime()))
			currentNum++;
		List<ColorHighlight> highlights = new ArrayList<>(3);
		int last = Integer.min(3, PANES_NUM - currentNum);
		for (int i = 0; i < last; i++) {
			int slotNum = orderedSlots[currentNum + i];
			if (slotNum == -1) continue;
			highlights.add(new ColorHighlight(slotNum, (224 - 64 * i) << 24 | 64 << 16 | 96 << 8 | 255));
		}
		return highlights;
	}

	public boolean orderSlots(Int2ObjectMap<ItemStack> slots) {
		ContainerSolver.trimEdges(slots, 4);
		orderedSlots = new int[PANES_NUM];
		Arrays.fill(orderedSlots, -1);
		for (Int2ObjectMap.Entry<ItemStack> slot : slots.int2ObjectEntrySet()) {
			if (slot.getValue().is(Items.STAINED_GLASS_PANE.black())) continue;
			if (slot.getValue().isEmpty()) {
				orderedSlots = null;
				return false;
			} else orderedSlots[slot.getValue().getCount() - 1] = slot.getIntKey();
		}
		currentNum = 0;
		return true;
	}

	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
		if (stack.isEmpty()) return false;

		if (!stack.is(Items.STAINED_GLASS_PANE.red()) || stack.getCount() != currentNum + 1) {
			return shouldBlockIncorrectClicks();
		}

		return false;
	}
}
