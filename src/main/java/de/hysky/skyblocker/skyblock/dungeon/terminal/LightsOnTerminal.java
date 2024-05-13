package de.hysky.skyblocker.skyblock.dungeon.terminal;

import java.util.List;

import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * The terminal where you change all the panes that are red to green.
 * 
 * This doesn't solve the terminal because you don't need a solver for it, but rather to simply allow for click blocking.
 */
public class LightsOnTerminal extends ContainerSolver implements TerminalSolver {
	private static final List<ColorHighlight> EMPTY = List.of();

	public LightsOnTerminal() {
		super("^Correct all the panes!$");
	}

	@Override
	protected boolean isEnabled() {
		return shouldBlockIncorrectClicks();
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		return EMPTY;
	}

	@Override
	protected boolean onClickSlot(int slot, ItemStack stack, int screenId, String[] groups) {
		return stack.isOf(Items.LIME_STAINED_GLASS_PANE);
	}
}
