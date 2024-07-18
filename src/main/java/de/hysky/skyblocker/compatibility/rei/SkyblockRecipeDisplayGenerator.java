package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class SkyblockRecipeDisplayGenerator implements DynamicDisplayGenerator<SkyblockRecipeDisplay> {

    @Override
    public Optional<List<SkyblockRecipeDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack entryStack)) return Optional.empty();
        return Optional.of(ItemRepository.getRecipes(entryStack).map(SkyblockRecipeDisplay::new).toList());
    }

    @Override
    public Optional<List<SkyblockRecipeDisplay>> getUsageFor(EntryStack<?> entry) {
        if (!(entry.getValue() instanceof ItemStack entryStack)) return Optional.empty();
        return Optional.of(ItemRepository.getUsages(entryStack).map(SkyblockRecipeDisplay::new).toList());
    }
}
