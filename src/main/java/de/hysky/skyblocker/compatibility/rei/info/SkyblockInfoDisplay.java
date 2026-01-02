package de.hysky.skyblocker.compatibility.rei.info;

import de.hysky.skyblocker.SkyblockerMod;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SkyblockInfoDisplay implements Display {
	private final ItemStack displayItem;

	public SkyblockInfoDisplay(ItemStack item) {
		this.displayItem = item;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredient.of(EntryStacks.of(displayItem)));
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		return List.of(EntryIngredient.of(EntryStacks.of(displayItem)));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(SkyblockerMod.id("skyblock_info"));
	}

	@Override
	public Optional<Identifier> getDisplayLocation() {
		return Optional.empty();
	}

	@Override
	public @Nullable DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}
}
