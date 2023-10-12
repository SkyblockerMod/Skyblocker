package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRegistry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.Items;

/**
 * REI integration
 */
public class SkyblockerREIClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<SkyblockCraftingDisplay> SKYBLOCK = CategoryIdentifier.of(SkyblockerMod.NAMESPACE, "skyblock");

    @Override
    public void registerCategories(CategoryRegistry categoryRegistry) {
        categoryRegistry.addWorkstations(SKYBLOCK, EntryStacks.of(Items.CRAFTING_TABLE));
        categoryRegistry.add(new SkyblockCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry displayRegistry) {
        displayRegistry.registerDisplayGenerator(SKYBLOCK, new SkyblockCraftingDisplayGenerator());
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.addEntries(ItemRegistry.getItemsStream().map(EntryStacks::of).toList());
    }
}
