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
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockNpcShopRecipe;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
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
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * REI integration
 */
public class SkyblockerREIClientPlugin implements REIClientPlugin {

	@Override
	public void registerCategories(CategoryRegistry categoryRegistry) {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		categoryRegistry.addWorkstations(CategoryIdentifier.of(SkyblockCraftingRecipe.ID), EntryStacks.of(Items.CRAFTING_TABLE));
		categoryRegistry.addWorkstations(CategoryIdentifier.of(SkyblockForgeRecipe.ID), EntryStacks.of(Items.ANVIL));
		categoryRegistry.addWorkstations(CategoryIdentifier.of(SkyblockNpcShopRecipe.ID), EntryStacks.of(Items.GOLD_NUGGET));

		categoryRegistry.add(new SkyblockRecipeCategory(SkyblockCraftingRecipe.ID, Text.translatable("emi.category.skyblocker.skyblock_crafting"), ItemUtils.getSkyblockerStack(), 73));
		categoryRegistry.add(new SkyblockRecipeCategory(SkyblockForgeRecipe.ID, Text.translatable("emi.category.skyblocker.skyblock_forge"), ItemUtils.getSkyblockerForgeStack(), 84));
		categoryRegistry.add(new SkyblockRecipeCategory(SkyblockNpcShopRecipe.ID, Text.translatable("emi.category.skyblocker.skyblock_npc_shop"), Items.GOLD_NUGGET.getDefaultStack(), 73));
		categoryRegistry.add(new SkyblockInfoCategory());
	}

	@Override
	public void registerDisplays(DisplayRegistry displayRegistry) {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		if (displayRegistry.getGlobalDisplayGenerators().stream().noneMatch(generator -> generator instanceof SkyblockRecipeDisplayGenerator))
			displayRegistry.registerGlobalDisplayGenerator(new SkyblockRecipeDisplayGenerator());
		if (displayRegistry.getGlobalDisplayGenerators().stream().noneMatch(generator -> generator instanceof SkyblockInfoDisplayGenerator))
			displayRegistry.registerGlobalDisplayGenerator(new SkyblockInfoDisplayGenerator());
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		if (!Utils.isOnSkyblock()) return;
		registry.registerFocusedStack(new SkyblockerFocusedStackProvider());
	}

	@Override
	public void registerEntries(EntryRegistry entryRegistry) {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		entryRegistry.removeEntryIf(entryStack -> true);
		entryRegistry.addEntries(ItemRepository.getItemsStream().map(EntryStacks::of).toList());
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
		if (!Utils.isOnSkyblock()) return;
		GeneralConfig.ItemList config = SkyblockerConfigManager.get().general.itemList;
		if (!config.enableItemList || !config.enableCollapsibleEntries) return;
		if (!ItemRepository.filesImported() || NEURepoManager.isLoading()) return;

		NEURepoManager.getConstants().getParents().getParents().forEach((parentId, childrenList) -> {
			Optional<ItemStack> parentItem = ItemRepository.getItemsStream().filter(itemStack -> itemStack.getNeuName().equals(parentId)).findFirst();
			if (parentItem.isEmpty()) return;

			List<EntryStack<ItemStack>> allItems = Stream.concat(parentItem.stream(), ItemRepository.getItemsStream().filter(itemStack -> childrenList.contains(itemStack.getNeuName())))
					.map(EntryStacks::of)
					.toList();

			String categoryPath = parentId.toLowerCase(Locale.ENGLISH).replace(";", "_");
			// Drop the tier at the end of the id so the category identifier remains the same even if the parent is changed to a different tier
			if (parentId.contains(";")) {
				categoryPath = categoryPath.substring(0, categoryPath.lastIndexOf("_"));
			}

			// For Enchanted Books, change the name of the category to the enchant name
			Text name;
			if (parentItem.get().isOf(Items.ENCHANTED_BOOK)) {
				String enchantName = ItemUtils.getLore(parentItem.get()).getFirst().getString();
				enchantName = enchantName.substring(0, enchantName.lastIndexOf(' ')); // drop level
				name = Text.literal(enchantName).formatted(parentId.startsWith("ULTIMATE") ? Formatting.LIGHT_PURPLE : Formatting.BLUE);
			} else {
				name = parentItem.get().getName();
			}

			registry.group(SkyblockerMod.id("rei_category/" + categoryPath), name, allItems);
		});
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		if (!Utils.isOnSkyblock()) return;
		zones.register(GenericContainerScreen.class, containerScreen -> {
			if (!SkyblockerConfigManager.get().uiAndVisuals.museumOverlay || !containerScreen.getTitle().getString().contains("Museum")) return List.of();
			HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
			return List.of(new Rectangle(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), MuseumManager.BACKGROUND_WIDTH, MuseumManager.BACKGROUND_HEIGHT));
		});

		zones.register(InventoryScreen.class, screen -> {
			if (!SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget || !Utils.isInGarden()) return List.of();
			HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
			return List.of(new Rectangle(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), 104, 127));
		});

		zones.register(Screen.class, screen -> {
			if (!VisitorHelper.shouldRender()) return List.of();
			return VisitorHelper.getExclusionZones().stream()
					.map(rect -> new Rectangle(rect.position().x(), rect.position().y(), rect.width(), rect.height()))
					.toList();
		});
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		if (!Utils.isOnSkyblock()) return;
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		registry.register(new SkyblockTransferHandler());
	}

	@Override
	public double getPriority() {
		return -50;
	}
}
