package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * The terminal where you change all the panes that are red to green.
 * <p>
 * This doesn't solve the terminal because you don't need a solver for it, but rather to simply allow for click blocking.
 */
public final class LightsOnTerminal extends ContainerSolver implements TerminalSolver {
	public LightsOnTerminal() {
		super("^Correct all the panes!$");
	}

	@Override
	protected boolean isEnabled() {
		return shouldBlockIncorrectClicks();
	}

	@Override
	protected boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		return stack.isOf(Items.LIME_STAINED_GLASS_PANE);
	}
}
