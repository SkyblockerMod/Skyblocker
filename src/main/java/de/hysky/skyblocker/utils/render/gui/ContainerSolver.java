package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import org.intellij.lang.annotations.Language;

import java.util.List;

/**
 * Abstract class for gui solvers. Extend this class to add a new gui solver, like terminal solvers or experiment solvers.
 */
public abstract class ContainerSolver extends AbstractContainerMatcher {
	protected ContainerSolver(@Language("RegExp") String titlePattern) {
		super(titlePattern);
	}

	protected abstract boolean isEnabled();

	protected void start(GenericContainerScreen screen) {
	}

	protected void reset() {
	}

	protected void markHighlightsDirty() {
		SkyblockerMod.getInstance().containerSolverManager.markDirty();
	}

	protected boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		return false;
	}

	protected List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}

	protected final void trimEdges(Int2ObjectMap<ItemStack> slots, int rows) {
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
