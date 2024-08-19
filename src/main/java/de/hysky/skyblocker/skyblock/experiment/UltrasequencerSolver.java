package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class UltrasequencerSolver extends ExperimentSolver {
	public static final UltrasequencerSolver INSTANCE = new UltrasequencerSolver();
	/**
	 * The slot id of the next slot to click.
	 */
	private int ultrasequencerNextSlot;

	private UltrasequencerSolver() {
		super("^Ultrasequencer \\(\\w+\\)$");
	}

	@Override
	protected boolean isEnabled(HelperConfig.Experiments experimentsConfig) {
		return experimentsConfig.enableUltrasequencerSolver;
	}

	/**
	 * Saves the shown items to {@link #slots the slot map}.
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	protected void tick(GenericContainerScreen screen) {
		switch (getState()) {
			case REMEMBER -> {
				Inventory inventory = screen.getScreenHandler().getInventory();
				if (inventory.getStack(49).getName().getString().equals("Remember the pattern!")) {
					for (int index = 9; index < 45; index++) {
						ItemStack itemStack = inventory.getStack(index);
						String name = itemStack.getName().getString();
						// Remember the item if its name is a number
						if (name.matches("\\d+")) {
							// Set the next slot to click to the item with the name "1"
							if (name.equals("1")) {
								ultrasequencerNextSlot = index;
							}
							// Save the item to the slot map
							getSlots().put(index, itemStack);
						}
					}
					setState(State.WAIT);
				}
			}
			case WAIT -> {
				if (screen.getScreenHandler().getInventory().getStack(49).getName().getString().startsWith("Timer: ")) {
					setState(State.SHOW);
					ContainerSolverManager.markHighlightsDirty();
				}
			}
			case END -> {
				String name = screen.getScreenHandler().getInventory().getStack(49).getName().getString();
				if (!name.startsWith("Timer: ")) {
					if (name.equals("Remember the pattern!")) {
						getSlots().clear();
						setState(State.REMEMBER);
					} else {
						reset();
					}
				}
			}
		}
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return getState() == State.SHOW && ultrasequencerNextSlot != 0 ? List.of(ColorHighlight.green(ultrasequencerNextSlot)) : List.of();
	}

	/**
	 * Finds the next slot to click via searching for the item stack count in {@link #slots the slot map} and sets {@link #ultrasequencerNextSlot} if the current slot was clicked.
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		if (getState() == State.SHOW && slot == ultrasequencerNextSlot) {
			int count = getSlots().get(ultrasequencerNextSlot).getCount() + 1;
			getSlots().int2ObjectEntrySet().stream()
					.filter(entry -> entry.getValue().getCount() == count)
					.findAny()
					.map(Int2ObjectMap.Entry::getIntKey)
					.ifPresentOrElse(nextSlot -> this.ultrasequencerNextSlot = nextSlot, () -> setState(ExperimentSolver.State.END));
		}
		return super.onClickSlot(slot, stack, screenId);
	}

	@Override
	public void reset() {
		ultrasequencerNextSlot = 0;
		super.reset();
	}
}
