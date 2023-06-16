package me.xmrvizzy.skyblocker.gui;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract class for gui solvers. Extend this class to add a new gui solver, like terminal solvers or experiment solvers.
 */
public abstract class ContainerSolver {
    private final Pattern CONTAINER_NAME;
    protected static final int RED_HIGHLIGHT = 64 << 24 | 255 << 16;
    protected static final int YELLOW_HIGHLIGHT = 128 << 24 | 255 << 16 | 255 << 8;
    protected static final int GREEN_HIGHLIGHT = 128 << 24 | 64 << 16 | 196 << 8 | 64;
    protected static final int GRAY_HIGHLIGHT = 128 << 24 | 64 << 16 | 64 << 8 | 64;

    protected ContainerSolver(String containerName) {
        CONTAINER_NAME = Pattern.compile(containerName);
    }

    protected abstract boolean isEnabled();

    public Pattern getName() {
        return CONTAINER_NAME;
    }

    protected void start(GenericContainerScreen screen) {
    }

    protected void reset() {
    }

    protected abstract List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots);

    protected void trimEdges(Map<Integer, ItemStack> slots, int rows) {
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
