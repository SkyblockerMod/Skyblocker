package me.xmrvizzy.skyblocker.gui;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract class for gui solvers. Extend this class to add a new gui solver, like terminal solvers or experiment solvers.
 */
public abstract class ContainerSolver {
    private final Pattern CONTAINER_NAME;
    protected final static int GREEN_HIGHLIGHT = 128 << 24 | 64 << 16 | 196 << 8 | 64;
    protected final static int GRAY_HIGHLIGHT = 128 << 24 | 64 << 16 | 64 << 8 | 64;

    public ContainerSolver(String containerName) {
        CONTAINER_NAME = Pattern.compile(containerName);
    }

    public abstract boolean isEnabled();

    public Pattern getName() {
        return CONTAINER_NAME;
    }

    public abstract List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots);

    public void trimEdges(Map<Integer, ItemStack> slots, int rows) {
        for (int i = 0; i < rows; i++) {
            slots.remove(9 * i);
            slots.remove(9 * i + 8);
        }
        for (int i = 1; i < 8; i++) {
            slots.remove(i);
            slots.remove((rows - 1) * 9 + i);
        }
    }
}
