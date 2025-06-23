package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;

import java.util.List;

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
        entryRegistry.addEntries(ItemRepository.getItemsStream().map(EntryStacks::of).toList());
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(InventoryScreen.class, screen -> {
            if (!SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget || !Utils.getLocation().equals(Location.GARDEN)) return List.of();
            HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
            return List.of(new Rectangle(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), 104, 127));
        });

        zones.register(Screen.class, screen -> {
            if (!VisitorHelper.shouldRender()) return List.of();
            return VisitorHelper.getExclusionZones();
        });
    }
}
