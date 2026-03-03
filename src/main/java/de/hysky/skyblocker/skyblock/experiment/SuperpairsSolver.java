package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class SuperpairsSolver extends ExperimentSolver {
	/**
	 * The slot id of the last clicked slot.
	 */
	private int superpairsPrevClickedSlot = 0;
	/**
	 * The item stack of the last clicked slot.
	 */
	private ItemStack superpairsCurrentSlot = ItemStack.EMPTY;
	/**
	 * The set of slot ids that contain duplicated items.
	 */
	private final IntSet superpairsDuplicatedSlots = new IntArraySet(28);

	public SuperpairsSolver() {
		super("^Superpairs \\(\\w+\\)$");
	}

	@Override
	protected boolean isEnabled(HelperConfig.Experiments experimentsConfig) {
		return experimentsConfig.enableSuperpairsSolver;
	}

	/**
	 * Sets the state to {@link State#SHOW} since Superpairs does not require a state machine.
	 * @param screen
	 */
	@Override
	public void start(ContainerScreen screen) {
		super.start(screen);
		setState(State.SHOW);
	}

	/**
	 * Checks the screen if the item of the last clicked slot is unknown.
	 * Adds duplicated items to the item of the last clicked slot to {@link #superpairsDuplicatedSlots},
	 * save the item of the last clicked slot to the slot map,
	 * and sets {@link #superpairsCurrentSlot} to the item of the last clicked slot.
	 */
	@Override
	protected void tick(ContainerScreen screen) {
		if (getState() == State.SHOW && getSlots().get(superpairsPrevClickedSlot) == null) {
			ItemStack itemStack = screen.getMenu().getContainer().getItem(superpairsPrevClickedSlot);
			if (!(itemStack.is(Items.CYAN_STAINED_GLASS) || itemStack.is(Items.BLACK_STAINED_GLASS_PANE) || itemStack.is(Items.AIR))) {
				getSlots().int2ObjectEntrySet().stream()
						.filter(entry -> ItemStack.matches(entry.getValue(), itemStack))
						.findAny()
						.ifPresent(entry -> {
							superpairsDuplicatedSlots.add(entry.getIntKey());
							superpairsDuplicatedSlots.add(superpairsPrevClickedSlot);
						});
				getSlots().put(superpairsPrevClickedSlot, itemStack);
				superpairsCurrentSlot = itemStack;
			}
		}
	}

	/**
	 * Displays a green highlight on the slot that matches {@link #superpairsCurrentSlot} and the experiment is waiting for a second click.
	 * Displays a yellow highlight on the slots that contain duplicated items.
	 * Displays a red highlight on the slots that do not match anything.
	 */
	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> displaySlots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		if (getState() == State.SHOW) {
			for (Int2ObjectMap.Entry<ItemStack> indexStack : displaySlots.int2ObjectEntrySet()) {
				int index = indexStack.getIntKey();
				ItemStack displayStack = indexStack.getValue();
				ItemStack stack = getSlots().get(index);
				if (stack != null && !ItemStack.matches(stack, displayStack)) {
					if (ItemStack.matches(superpairsCurrentSlot, stack) && displayStack.getHoverName().getString().equals("Click a second button!")) {
						highlights.add(ColorHighlight.green(index));
					} else if (superpairsDuplicatedSlots.contains(index)) {
						highlights.add(ColorHighlight.yellow(index));
					} else {
						highlights.add(ColorHighlight.red(index));
					}
				}
			}
		}
		return highlights;
	}

	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
		if (getState() == State.SHOW) {
			this.superpairsPrevClickedSlot = slot;
			this.superpairsCurrentSlot = ItemStack.EMPTY;
		}
		return super.onClickSlot(slot, stack, screenId, button);
	}

	@Override
	public void reset() {
		superpairsPrevClickedSlot = 0;
		superpairsCurrentSlot = ItemStack.EMPTY;
		superpairsDuplicatedSlots.clear();
		super.reset();
	}
}
