package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.compatibility.rei.info.SkyblockInfoCategory;
import de.hysky.skyblocker.compatibility.rei.info.SkyblockInfoDisplayGenerator;
import de.hysky.skyblocker.compatibility.rei.recipe.SkyblockRecipeCategory;
import de.hysky.skyblocker.compatibility.rei.recipe.SkyblockRecipeDisplayGenerator;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

		categoryRegistry.add(new SkyblockInfoCategory());
	}

	@Override
	public void registerDisplays(DisplayRegistry displayRegistry) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		if (displayRegistry.getGlobalDisplayGenerators().stream().noneMatch(generator -> generator instanceof SkyblockRecipeDisplayGenerator))
			displayRegistry.registerGlobalDisplayGenerator(new SkyblockRecipeDisplayGenerator());
		if (displayRegistry.getGlobalDisplayGenerators().stream().noneMatch(generator -> generator instanceof SkyblockInfoDisplayGenerator))
			displayRegistry.registerGlobalDisplayGenerator(new SkyblockInfoDisplayGenerator());
	}

	@Override
	public void registerEntries(EntryRegistry entryRegistry) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		entryRegistry.removeEntryIf(entryStack -> true);
		entryRegistry.addEntries(ItemRepository.getItemsStream().map(EntryStacks::of).toList());
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
		GeneralConfig.ItemList config = SkyblockerConfigManager.get().general.itemList;
		if (!config.enableItemList || !config.enableCollapsibleEntries) return;
		if (!ItemRepository.filesImported() || NEURepoManager.isLoading()) return;

		NEURepoManager.getConstants().getParents().getParents().forEach((parentId, childrenList) -> {
			Optional<ItemStack> parentItem = ItemRepository.getItemsStream().filter(itemStack -> itemStack.getNeuName().equals(parentId)).findFirst();
			if (parentItem.isEmpty()) return;

			List<EntryStack<ItemStack>> allItems = Stream.concat(parentItem.stream(), ItemRepository.getItemsStream().filter(itemStack -> childrenList.contains(itemStack.getNeuName())))
					.map(EntryStacks::of)
					.toList();
			registry.group(Identifier.of(SkyblockerMod.NAMESPACE, "rei_category/" + parentId.toLowerCase().replace(";", "_")), parentItem.get().getName(), allItems);
		});
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(GenericContainerScreen.class, containerScreen -> {
			if (!SkyblockerConfigManager.get().uiAndVisuals.museumOverlay || !containerScreen.getTitle().getString().contains("Museum")) return List.of();
			HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
			return List.of(new Rectangle(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), MuseumManager.BACKGROUND_WIDTH, MuseumManager.BACKGROUND_HEIGHT));
		});

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
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		registry.register(new SkyblockTransferHandler());
	}

	@Override
	public double getPriority() {
		return -50;
	}
}
