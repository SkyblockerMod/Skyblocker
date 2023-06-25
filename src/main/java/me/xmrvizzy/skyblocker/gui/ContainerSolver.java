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
    private final Pattern containerName;

    protected ContainerSolver(String containerName) {
        this.containerName = Pattern.compile(containerName);
    }

    protected abstract boolean isEnabled();

    public Pattern getName() {
        return containerName;
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
