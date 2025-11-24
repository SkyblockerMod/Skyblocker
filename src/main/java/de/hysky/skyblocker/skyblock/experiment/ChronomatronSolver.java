package de.hysky.skyblocker.skyblock.experiment;

import de.hysky.skyblocker.config.configs.HelperConfig;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

import java.util.ArrayList;
import java.util.List;

public final class ChronomatronSolver extends ExperimentSolver implements ScreenHandlerListener {
	public static final Object2ObjectMap<Item, Item> TERRACOTTA_TO_GLASS = Object2ObjectMaps.unmodifiable(
			new Object2ObjectArrayMap<>(
					new Item[]{
							Items.RED_TERRACOTTA, Items.ORANGE_TERRACOTTA, Items.YELLOW_TERRACOTTA, Items.LIME_TERRACOTTA, Items.GREEN_TERRACOTTA, Items.CYAN_TERRACOTTA, Items.LIGHT_BLUE_TERRACOTTA, Items.BLUE_TERRACOTTA, Items.PURPLE_TERRACOTTA, Items.PINK_TERRACOTTA
					},
					new Item[]{
							Items.RED_STAINED_GLASS, Items.ORANGE_STAINED_GLASS, Items.YELLOW_STAINED_GLASS, Items.LIME_STAINED_GLASS, Items.GREEN_STAINED_GLASS, Items.CYAN_STAINED_GLASS, Items.LIGHT_BLUE_STAINED_GLASS, Items.BLUE_STAINED_GLASS, Items.PURPLE_STAINED_GLASS, Items.PINK_STAINED_GLASS
					}
			)
	);

	private GenericContainerScreen screen;

	/**
	 * The list of items to remember, in order.
	 */
	private final List<Item> chronomatronSlots = new ArrayList<>();
	/**
	 * The index of the current item shown in the chain, used for remembering.
	 */
	private int chronomatronChainLengthCount;
	/**
	 * The slot id of the current item shown, used for detecting when the experiment finishes showing the current item.
	 */
	private int chronomatronCurrentSlot;
	/**
	 * The next index in the chain to click.
	 */
	private int chronomatronCurrentOrdinal;

	/**
	 * Whether the board is a single row or two rows.
	 * The first 3 Chronomatron levels are a single row.
	 * The remaining 2 are two rows.
	 */
	private boolean isSingleRow;

	public ChronomatronSolver() {
		super("^Chronomatron \\(\\w+\\)$");
	}

	@Override
	protected boolean isEnabled(HelperConfig.Experiments experimentsConfig) {
		return experimentsConfig.enableChronomatronSolver;
	}

	@Override
	protected void tick(GenericContainerScreen screen) {
	}

	/**
	 * Only process the changes for items in the center row (one or two rows, depending on the Chronomatron level), and for the instruction/clock item
	 */
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
		if (slotId < 17 || slotId > (isSingleRow ? 25 : 34) && slotId != 49) return;
		switch (getState()) {
			case REMEMBER -> {
				if (slotId == 49) break;
				// Only try to look for items with enchantment glint if there is no item being currently shown.
				if (chronomatronCurrentSlot == 0) {
					if (stack.hasGlint()) {
						// If the list of items is smaller than the index of the current item shown, add the item to the list and set the state to wait.
						if (chronomatronSlots.size() <= chronomatronChainLengthCount) {
							chronomatronSlots.add(TERRACOTTA_TO_GLASS.get(stack.getItem()));
							setState(State.WAIT);
						} else {
							// If the item is already in the list, increment the current item shown index.
							chronomatronChainLengthCount++;
						}
						// Remember the slot shown to detect when the experiment finishes showing the current item.
						chronomatronCurrentSlot = slotId;
					}
					// If the current item shown no longer has enchantment glint, the experiment finished showing the current item.
				} else if (chronomatronCurrentSlot == slotId && !stack.hasGlint()) {
					chronomatronCurrentSlot = 0;
				}
			}
			case WAIT -> {
				if (slotId == 49 && stack.getName().getString().startsWith("Timer: ")) {
					setState(State.SHOW);
				}
			}
			case END -> {
				String name = stack.getName().getString();
				if (slotId == 49 && !name.startsWith("Timer: ")) {
					// Get ready for another round if the instructions say to remember the pattern.
					if (name.equals("Remember the pattern!")) {
						chronomatronChainLengthCount = 0;
						chronomatronCurrentOrdinal = 0;
						setState(State.REMEMBER);
					} else {
						reset();
					}
				}
			}
		}
	}

	/**
	 * Highlights the slots that contain the item at index {@link #chronomatronCurrentOrdinal} of {@link #chronomatronSlots} in the chain.
	 */
	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		if (getState() == State.SHOW && chronomatronSlots.size() > chronomatronCurrentOrdinal) {
			for (Int2ObjectMap.Entry<ItemStack> indexStack : slots.int2ObjectEntrySet()) {
				int index = indexStack.getIntKey();
				ItemStack stack = indexStack.getValue();
				Item item = chronomatronSlots.get(chronomatronCurrentOrdinal);
				if (stack.isOf(item) || TERRACOTTA_TO_GLASS.get(stack.getItem()) == item) {
					highlights.add(ColorHighlight.green(index));
				}
			}
		}
		return highlights;
	}

	/**
	 * Increments {@link #chronomatronCurrentOrdinal} if the item clicked matches the item at {@link #chronomatronCurrentOrdinal the current index} in the chain.
	 */
	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
		if (getState() == State.SHOW) {
			Item item = chronomatronSlots.get(chronomatronCurrentOrdinal);
			if ((stack.isOf(item) || ChronomatronSolver.TERRACOTTA_TO_GLASS.get(stack.getItem()) == item)) {
				if (++chronomatronCurrentOrdinal >= chronomatronSlots.size()) {
					setState(ExperimentSolver.State.END);
				}
			} else {
				return shouldBlockIncorrectClicks();
			}
		}
		return super.onClickSlot(slot, stack, screenId, button);
	}

	@Override
	public void start(GenericContainerScreen screen) {
		super.start(screen);
		this.screen = screen;
		screen.getScreenHandler().addListener(this);
		String title = screen.getTitle().getString();
		isSingleRow = title.endsWith("(High)") || title.endsWith("(Grand)") || title.endsWith("(Supreme)");
	}

	@Override
	public void reset() {
		chronomatronSlots.clear();
		chronomatronChainLengthCount = 0;
		chronomatronCurrentSlot = 0;
		chronomatronCurrentOrdinal = 0;
		if (screen != null) screen.getScreenHandler().removeListener(this);
		super.reset();
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}
}
