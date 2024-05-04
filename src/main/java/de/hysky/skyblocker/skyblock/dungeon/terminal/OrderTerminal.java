package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderTerminal extends ContainerSolver {
    private final int PANES_NUM = 14;
    private int[] orderedSlots;
    private int currentNum = Integer.MAX_VALUE;

    public OrderTerminal() {
        super("^Click in order!$");
    }

    @Override
    protected boolean isEnabled() {
        orderedSlots = null;
        currentNum = 0;
        return SkyblockerConfigManager.get().locations.dungeons.terminals.solveOrder;
    }

    @Override
    protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
        if(orderedSlots == null && !orderSlots(slots))
            return Collections.emptyList();
        while(currentNum < PANES_NUM && Items.LIME_STAINED_GLASS_PANE.equals(slots.get(orderedSlots[currentNum]).getItem()))
            currentNum++;
        List<ColorHighlight> highlights = new ArrayList<>(3);
        int last = Integer.min(3, PANES_NUM - currentNum);
        for(int i = 0; i < last; i++) {
            highlights.add(new ColorHighlight(orderedSlots[currentNum + i], (224 - 64 * i) << 24 | 64 << 16 | 96 << 8 | 255));
        }
        return highlights;
    }

    public boolean orderSlots(Int2ObjectMap<ItemStack> slots) {
        trimEdges(slots, 4);
        orderedSlots = new int[PANES_NUM];
        for(Int2ObjectMap.Entry<ItemStack> slot : slots.int2ObjectEntrySet()) {
            if(Items.AIR.equals(slot.getValue().getItem())) {
                orderedSlots = null;
                return false;
            }
            else
                orderedSlots[slot.getValue().getCount() - 1] = slot.getIntKey();
        }
        currentNum = 0;
        return true;
    }
}