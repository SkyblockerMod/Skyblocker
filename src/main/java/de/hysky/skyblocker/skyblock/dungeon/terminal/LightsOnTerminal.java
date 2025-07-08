package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

/**
 * The terminal where you change all the panes that are red to green.
 *
 * This doesn't solve the terminal because you don't need a solver for it, but rather to simply allow for click blocking.
 */
public final class LightsOnTerminal extends SimpleContainerSolver implements TerminalSolver {
	private static final List<ColorHighlight> EMPTY = List.of();

	public LightsOnTerminal() {
		super("^Correct all the panes!$");
	}

	@Override
	public boolean isEnabled() {
		return shouldBlockIncorrectClicks();
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return EMPTY;
	}

	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		return stack.isOf(Items.LIME_STAINED_GLASS_PANE);
	}
}
