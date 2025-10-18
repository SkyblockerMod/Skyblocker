package de.hysky.skyblocker.compatibility.rei.info;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class SkyblockInfoDisplayGenerator implements DynamicDisplayGenerator<SkyblockInfoDisplay> {
	@Override
	public Optional<List<SkyblockInfoDisplay>> getRecipeFor(EntryStack<?> entry) {
		if (!(entry.getValue() instanceof ItemStack entryStack)) return Optional.empty();
		if (entryStack.getSkyblockId().isEmpty()) return Optional.empty();
		ItemStack stack = entryStack.copy();
		stack.setCount(1);
		return Optional.of(List.of(new SkyblockInfoDisplay(stack)));
	}

	@Override
	public Optional<List<SkyblockInfoDisplay>> getUsageFor(EntryStack<?> entry) {
		return getRecipeFor(entry);
	}
}
