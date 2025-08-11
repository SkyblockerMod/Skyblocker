package de.hysky.skyblocker.skyblock.dungeon.terminal;

import com.google.common.collect.ImmutableMap;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Solver for the "Change all to same color!" terminal.
 * It displays the minimal number of clicks required to change each pane
 * to the most common color in the grid.
 */
public final class SameColorTerminal extends SimpleContainerSolver implements TerminalSolver, SlotTextAdder {
	public static final SameColorTerminal INSTANCE = new SameColorTerminal();
	private static final Item[] ORDER = new Item[]{
			Items.RED_STAINED_GLASS_PANE,
			Items.ORANGE_STAINED_GLASS_PANE,
			Items.YELLOW_STAINED_GLASS_PANE,
			Items.GREEN_STAINED_GLASS_PANE,
			Items.BLUE_STAINED_GLASS_PANE
	};
	private static final ImmutableMap<Item, Integer> INDEX;

	static {
		ImmutableMap.Builder<Item, Integer> builder = ImmutableMap.builderWithExpectedSize(ORDER.length);
		for (int i = 0; i < ORDER.length; i++) {
			builder.put(ORDER[i], i);
		}
		INDEX = builder.build();
	}

	private final Int2IntMap clickMap = new Int2IntOpenHashMap();

	private SameColorTerminal() {
		super("^Change all to same color!$");
	}

	@Override
	public boolean isEnabled() {
		clickMap.clear();
		return SkyblockerConfigManager.get().dungeons.terminals.solveSameColor;
	}

	private void computeClicks(Int2ObjectMap<ItemStack> slots) {
		clickMap.clear();
		int[] counts = new int[ORDER.length];
		Int2IntMap slotColors = new Int2IntOpenHashMap();

		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			Item item = entry.getValue().getItem();
			Integer idx = INDEX.get(item);
			if (idx != null) {
				slotColors.put(entry.getIntKey(), idx.intValue());
				counts[idx]++;
			}
		}
		if (slotColors.isEmpty()) {
			return;
		}

		int target = 0;
		for (int i = 1; i < counts.length; i++) {
			if (counts[i] > counts[target]) target = i;
		}

		for (Int2IntMap.Entry entry : slotColors.int2IntEntrySet()) {
			int slot = entry.getIntKey();
			int color = entry.getIntValue();
			int diffForward = Math.floorMod(target - color, ORDER.length);
			int diffBackward = Math.floorMod(color - target, ORDER.length);
			int clicks = diffForward <= diffBackward ? diffForward : -diffBackward;
			clickMap.put(slot, clicks);
		}
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		computeClicks(slots);
		return List.of();
	}

	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId) {
		if (clickMap.containsKey(slot) && clickMap.get(slot) == 0) {
			return shouldBlockIncorrectClicks();
		}
		return false;
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		int clicks = clickMap.getOrDefault(slotId, 0);
		if (clicks == 0) return List.of();
		return SlotText.topLeftList(Text.literal(String.valueOf(clicks)).formatted(Formatting.GOLD));
	}
}
