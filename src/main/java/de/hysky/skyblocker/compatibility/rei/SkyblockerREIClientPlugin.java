package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

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
        categoryRegistry.add(new SkyblockRecipeCategory(SkyblockForgeRecipe.IDENTIFIER, Text.translatable("emi.category.skyblocker.skyblock_forge"), new ItemStack(Items.FURNACE), 84));
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
	public double getPriority() {
		return 4096;
	}
}
