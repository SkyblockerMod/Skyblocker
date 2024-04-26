package de.hysky.skyblocker.skyblock.dungeon.terminal;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StartsWithTerminal extends ContainerSolver {
	private final Int2ObjectOpenHashMap<ItemState> trackedItemStates = new Int2ObjectOpenHashMap<>();

	public StartsWithTerminal() {
		super("^What starts with: '([A-Z])'\\?$");
	}

	@Override
	protected boolean isEnabled() {
		return SkyblockerConfigManager.get().locations.dungeons.terminals.solveStartsWith;
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		trimEdges(slots, 6);
		setupState(slots);

		String prefix = groups[0];
		List<ColorHighlight> highlights = new ArrayList<>();

		for (Int2ObjectMap.Entry<ItemStack> slot : slots.int2ObjectEntrySet()) {
			ItemStack stack = slot.getValue();
			ItemState state = trackedItemStates.getOrDefault(slot.getIntKey(), ItemState.DEFAULT);

			//If the item hasn't been marked as clicked and it matches the starts with condition
			//We keep track of the clicks ourselves instead of using the enchantment glint because some items like nether stars have the glint override component by default
			//so even if Hypixel tries to change that to the same thing it was before (true) it won't work and the solver would permanently consider the item to be clicked
			//even if it hasn't been yet
			if (!state.clicked() && stack.getName().getString().startsWith(prefix)) {
				highlights.add(ColorHighlight.green(slot.getIntKey()));
			}
		}
		return highlights;
	}

	@Override
	protected void onClickSlot(int slot, ItemStack stack, ItemStack cursorStack, String[] groups) {
		//Some random glass pane was clicked or something
		if (!trackedItemStates.containsKey(slot) || stack == null || stack.isEmpty()) return;

		ItemState state = trackedItemStates.get(slot);
		String prefix = groups[0];

		//If the item stack's name starts with the correct letter
		//Also, since Hypixel closes & reopens the GUI after every click we check if the cursor stack is empty to avoid marking item's as clicked if the server lags and
		//you can click multiple things before the GUI closes - in this event only the first click item click is counted
		if (stack.getName().getString().startsWith(prefix) && (cursorStack == null || cursorStack.isEmpty()) && !state.clicked()) {
			trackedItemStates.put(slot, state.click());
		}
		//In the future we could add an else branch and return a boolean to cancel the click since it would be wrong

		return;
	}

	//We only setup the state when all items aren't null or empty. This prevents the state from being reset due to unsent items or server lag spikes/bad TPS (fix ur servers Hypixel)
	private void setupState(Int2ObjectMap<ItemStack> usefulSlots) {
		Predicate<Int2ObjectMap.Entry<ItemStack>> notNullOrEmpty = e -> e.getValue() != null && !e.getValue().isEmpty();

		if (allEntriesMatch(usefulSlots.int2ObjectEntrySet(), notNullOrEmpty)) {
			//If the state hasn't been setup then we will do that
			if (trackedItemStates.isEmpty()) {
				for (Int2ObjectMap.Entry<ItemStack> entry : usefulSlots.int2ObjectEntrySet()) {
					trackedItemStates.put(entry.getIntKey(), ItemState.of(entry.getValue().getItem()));
				}
			} else { //If the state is setup then we verify that it hasn't changed since last time, and if it has then we will clear it and call this method again to set it up
				//Checks whether the trackedItemStates contains the slot id and if it does it checks whether the tracked state's item is a 1:1 match
				Predicate<Int2ObjectMap.Entry<ItemStack>> doesItemMatch = e -> trackedItemStates.containsKey(e.getIntKey()) && trackedItemStates.get(e.getIntKey()).itemMatches(e.getValue().getItem());

				if (!allEntriesMatch(usefulSlots.int2ObjectEntrySet(), doesItemMatch)) {
					clearState();
					setupState(usefulSlots);
				}
			}
		}
	}

	private void clearState() {
		trackedItemStates.clear();
	}

	private static boolean allEntriesMatch(ObjectSet<Int2ObjectMap.Entry<ItemStack>> entries, Predicate<Int2ObjectMap.Entry<ItemStack>> predicate) {
		for (Int2ObjectMap.Entry<ItemStack> entry : entries) {
			if (!predicate.test(entry)) return false;
		}

		return true;
	}

	private record ItemState(Item item, boolean clicked) {
		private static final ItemState DEFAULT = new ItemState(null, false);

		boolean itemMatches(Item item) {
			return this.item.equals(item);
		}

		ItemState click() {
			return new ItemState(item, true);
		}

		static ItemState of(Item item) {
			return new ItemState(item, false);
		}
	}
}