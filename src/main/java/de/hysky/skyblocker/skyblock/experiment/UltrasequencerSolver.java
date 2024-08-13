package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class UltrasequencerSolver extends ExperimentSolver {
	public static final UltrasequencerSolver INSTANCE = new UltrasequencerSolver();
	private int ultrasequencerNextSlot;

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

	private UltrasequencerSolver() {
		super("^Ultrasequencer \\(\\w+\\)$");
	}

	@Override
	protected boolean isEnabled(HelperConfig.Experiments experimentsConfig) {
		return experimentsConfig.enableUltrasequencerSolver;
	}

	@Override
	protected void tick(GenericContainerScreen screen) {
		switch (getState()) {
			case REMEMBER -> {
				Inventory inventory = screen.getScreenHandler().getInventory();
				if (inventory.getStack(49).getName().getString().equals("Remember the pattern!")) {
					for (int index = 9; index < 45; index++) {
						ItemStack itemStack = inventory.getStack(index);
						String name = itemStack.getName().getString();
						if (name.matches("\\d+")) {
							if (name.equals("1")) {
								ultrasequencerNextSlot = index;
							}
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
		return getState() == State.SHOW && ultrasequencerNextSlot != 0 ? List.of(ColorHighlight.green(ultrasequencerNextSlot)) : new ArrayList<>();
	}
}
