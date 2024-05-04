package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;
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

    protected void markHighlightsDirty() {
        SkyblockerMod.getInstance().containerSolverManager.markDirty();
    }

    protected void onClickSlot(int slot, ItemStack stack, int screenId, String[] groups) {
    }

    protected abstract List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots);

    protected void trimEdges(Int2ObjectMap<ItemStack> slots, int rows) {
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
