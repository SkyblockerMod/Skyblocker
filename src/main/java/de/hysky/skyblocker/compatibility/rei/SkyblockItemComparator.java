package de.hysky.skyblocker.compatibility.rei;

import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import net.minecraft.item.ItemStack;

public class SkyblockItemComparator implements EntryComparator<ItemStack> {
	@Override
	public long hash(ComparisonContext context, ItemStack itemStack) {
		if (itemStack.getNeuName().isEmpty()) {
			return EntryComparator.noop().hash(context, itemStack);
		}
		return itemStack.getNeuName().hashCode();
	}
}
