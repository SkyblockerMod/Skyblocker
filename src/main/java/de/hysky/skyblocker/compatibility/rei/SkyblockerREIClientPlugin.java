package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
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
import net.minecraft.text.Text;

import java.util.List;

/**
 * REI integration
 */
public class SkyblockerREIClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry categoryRegistry) {
        if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
        categoryRegistry.addWorkstations(CategoryIdentifier.of(SkyblockCraftingRecipe.IDENTIFIER), EntryStacks.of(Items.CRAFTING_TABLE));
        categoryRegistry.addWorkstations(CategoryIdentifier.of(SkyblockForgeRecipe.IDENTIFIER), EntryStacks.of(Items.ANVIL));
        categoryRegistry.add(new SkyblockRecipeCategory(SkyblockCraftingRecipe.IDENTIFIER, Text.translatable("emi.category.skyblocker.skyblock_crafting"), ItemUtils.getSkyblockerStack(), 73));
        categoryRegistry.add(new SkyblockRecipeCategory(SkyblockForgeRecipe.IDENTIFIER, Text.translatable("emi.category.skyblocker.skyblock_forge"), ItemUtils.getSkyblockerForgeStack(), 84));
    }

    @Override
    public void registerDisplays(DisplayRegistry displayRegistry) {
        if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
        displayRegistry.registerGlobalDisplayGenerator(new SkyblockRecipeDisplayGenerator());
    }

    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
        entryRegistry.removeEntryIf(entryStack -> true);
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

	@Override
	public double getPriority() {
		return 4096;
	}
}
