package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class RaffleTaskHighlight extends SimpleContainerSolver {
	public RaffleTaskHighlight() {
		super("Daily Tasks");
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		ObjectArrayList<ColorHighlight> highlights = new ObjectArrayList<>(21); // There are 21 paper slots, and they are either complete or incomplete, so we're guaranteed to need 21 highlights

		ObjectSet<Entry<ItemStack>> set = slots.int2ObjectEntrySet();
		for (Entry<ItemStack> entry : set) {
			ItemStack itemStack = entry.getValue();
			if (!itemStack.isOf(Items.PAPER) && !itemStack.isOf(Items.MAP) && !itemStack.isOf(Items.FILLED_MAP)) continue;
			List<String> lore = itemStack.skyblocker$getLoreStrings();
			if (lore.isEmpty()) continue;
			switch (lore.getLast()) {
				case "COMPLETE" -> highlights.add(ColorHighlight.green(entry.getIntKey()));
				case "INCOMPLETE" -> highlights.add(ColorHighlight.red(entry.getIntKey()));
			}
		}

		return highlights;
	}
}
