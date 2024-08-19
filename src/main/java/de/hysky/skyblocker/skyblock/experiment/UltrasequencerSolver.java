package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.DyeColor;

import java.util.List;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

public final class UltrasequencerSolver extends ExperimentSolver {
	public static final UltrasequencerSolver INSTANCE = new UltrasequencerSolver();
	/**
	 * The playable slots of Ultrasequencer in the Metaphysical level.
	 * 
	 * Even though the Supreme/Transcendent levels have less playable slots we filter out black glass panes later on
	 * since black isn't in the color sequence.
	 */
	private static final int[] PANE_SLOTS = IntStream.rangeClosed(9, 44).toArray();

	/**
	 * The slot id of the next slot to click.
	 */
	private int ultrasequencerNextSlot;
	/**
	 * Saves the {@link DyeColor} instance corresponding to the color of the pane showed in the screen as it changes each round.
	 * Used for detecting when the round ends.
	 */
	@Nullable
	private DyeColor lastColor;

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
					// Note: This relies on Hypixel sending items sequentially, hopefully this doesn't change in the future
					setState(State.WAIT);
				}
			}
			case WAIT -> {
				if (screen.getScreenHandler().getInventory().getStack(49).getName().getString().startsWith("Timer: ")) {
					setState(State.SHOW);
					//This doesn't trigger the markDirty method in this class as the pane color is already updated
					//as the chain goes END -> REMEMBER -> WAIT
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
					.ifPresent(nextSlot -> this.ultrasequencerNextSlot = nextSlot);
		}
		return super.onClickSlot(slot, stack, screenId);
	}

	/**
	 * Keeps track of when rounds end. The state is set to END when the colour of the glass panes change (excluding the border panes) and we aren't on the first round.
	 */
	@Override
	public void markDirty() {
		if (MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen genericContainerScreen) {
			List<Slot> slots = genericContainerScreen.getScreenHandler().slots.subList(0, genericContainerScreen.getScreenHandler().getRows() * 9);
			Int2ObjectMap<ItemStack> slotMap = ContainerSolverManager.slotMap(slots);

			for (int paneSlot : PANE_SLOTS) {
				ItemStack slotItem = slotMap.get(paneSlot);

				if (slotItem != null && !slotItem.isEmpty() && slotItem.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof StainedGlassPaneBlock stainedGlassPaneBlock) {
					DyeColor paneColor = stainedGlassPaneBlock.getColor();

					// Filter out black stained glass panes that border the solver
					if (paneColor == DyeColor.BLACK) continue;

					if (lastColor != paneColor) {
						// Null check to prevent setting the state to END when its showing the first sequence
						if (lastColor != null) setState(State.END);

						lastColor = paneColor;

						return;
					}
				}
			}
		}
	}

	@Override
	public void reset() {
		ultrasequencerNextSlot = 0;
		lastColor = null;
		super.reset();
	}
}
