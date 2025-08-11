package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OrderTerminal extends SimpleContainerSolver implements TerminalSolver {
    private static final int PANES_NUM = 14;
    private int[] orderedSlots;
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
        while (currentNum < PANES_NUM && slots.containsKey(orderedSlots[currentNum]) && Items.LIME_STAINED_GLASS_PANE.equals(slots.get(orderedSlots[currentNum]).getItem()))
            currentNum++;
        List<ColorHighlight> highlights = new ArrayList<>(3);
        int last = Integer.min(3, PANES_NUM - currentNum);
        for (int i = 0; i < last; i++) {
            highlights.add(new ColorHighlight(orderedSlots[currentNum + i], (224 - 64 * i) << 24 | 64 << 16 | 96 << 8 | 255));
        }
        return highlights;
    }

    public boolean orderSlots(Int2ObjectMap<ItemStack> slots) {
        ContainerSolver.trimEdges(slots, 4);
        orderedSlots = new int[PANES_NUM];
        for (Int2ObjectMap.Entry<ItemStack> slot : slots.int2ObjectEntrySet()) {
            if (Items.AIR.equals(slot.getValue().getItem())) {
                orderedSlots = null;
                return false;
            }
            else orderedSlots[slot.getValue().getCount() - 1] = slot.getIntKey();
        }
        currentNum = 0;
        return true;
    }

    @Override
    public boolean onClickSlot(int slot, ItemStack stack, int screenId) {
        if (stack == null || stack.isEmpty()) return false;

        if (!stack.isOf(Items.RED_STAINED_GLASS_PANE) || stack.getCount() != currentNum + 1) {
            return shouldBlockIncorrectClicks();
        }

        return false;
    }
}
