package de.hysky.skyblocker.compatibility.jei;

import java.util.List;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.garden.visitor.VisitorHelper;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockNpcShopRecipe;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import mezz.jei.library.load.registration.SubtypeRegistration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@JeiPlugin
public class SkyblockerJEIPlugin implements IModPlugin {
	private static final boolean JEI_LOADED = FabricLoader.getInstance().isModLoaded("jei");
	private SkyblockCraftingRecipeCategory skyblockCraftingRecipeCategory;
	private SkyblockForgeRecipeCategory skyblockForgeRecipeCategory;
	private SkyblockNpcShopRecipeCategory skyblockNpcShopRecipeCategory;

	public static void trickJEIIntoLoadingRecipes() {
		if (JEI_LOADED) {
			JeiLifecycleEvents.AFTER_RECIPE_SYNC.invoker().run();
		}
	}

	@Override
	public Identifier getPluginUid() {
		return SkyblockerMod.id("skyblock");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		SubtypeInterpreters interpreters = ((SubtypeRegistration) registration).getInterpreters();
		ItemRepository.getItemsStream().filter(stack -> !interpreters.contains(VanillaTypes.ITEM_STACK, stack)).map(ItemStack::getItem).distinct().forEach(item ->
		registration.registerSubtypeInterpreter(item, (stack, context) -> ItemStackComponentizationFixer.componentsAsString(stack))
				);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		this.skyblockCraftingRecipeCategory = new SkyblockCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper());
		registration.addRecipeCategories(this.skyblockCraftingRecipeCategory);

		this.skyblockForgeRecipeCategory = new SkyblockForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper());
		registration.addRecipeCategories(this.skyblockForgeRecipeCategory);

		this.skyblockNpcShopRecipeCategory = new SkyblockNpcShopRecipeCategory(registration.getJeiHelpers().getGuiHelper());
		registration.addRecipeCategories(this.skyblockNpcShopRecipeCategory);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(GenericContainerScreen.class, new GenericContainerHandler());
		registration.addGuiContainerHandler(InventoryScreen.class, new InventoryContainerHandler());
		registration.addGlobalGuiHandler(new GlobalHandler());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, ItemRepository.getItems());
		registration.addRecipes(this.skyblockCraftingRecipeCategory.getRecipeType(), ItemRepository.getRecipesStream().filter(SkyblockCraftingRecipe.class::isInstance).map(SkyblockCraftingRecipe.class::cast).toList());
		registration.addRecipes(this.skyblockForgeRecipeCategory.getRecipeType(), ItemRepository.getRecipesStream().filter(SkyblockForgeRecipe.class::isInstance).map(SkyblockForgeRecipe.class::cast).toList());
		registration.addRecipes(this.skyblockNpcShopRecipeCategory.getRecipeType(), ItemRepository.getRecipesStream().filter(SkyblockNpcShopRecipe.class::isInstance).map(SkyblockNpcShopRecipe.class::cast).toList());
	}

	private static class GenericContainerHandler implements IGuiContainerHandler<GenericContainerScreen> {
		@Override
		public List<Rect2i> getGuiExtraAreas(GenericContainerScreen containerScreen) {
			if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.museumOverlay || !containerScreen.getTitle().getString().contains("Museum")) return List.of();
			HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
			return List.of(new Rect2i(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), MuseumManager.BACKGROUND_WIDTH, MuseumManager.BACKGROUND_HEIGHT));
		}
	}

	private static class InventoryContainerHandler implements IGuiContainerHandler<InventoryScreen> {
		@Override
		public List<Rect2i> getGuiExtraAreas(InventoryScreen containerScreen) {
			if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget || !Utils.isInGarden()) return List.of();
			HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
			return List.of(new Rect2i(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), 104, 127));
		}
	}

	private static class GlobalHandler implements IGlobalGuiHandler {
		@Override
		public List<Rect2i> getGuiExtraAreas() {
			if (!Utils.isOnSkyblock() || !VisitorHelper.shouldRender()) return List.of();
			return VisitorHelper.getExclusionZones().stream()
					.map(rect -> new Rect2i(rect.position().x(), rect.position().y(), rect.width(), rect.height()))
					.toList();
		}
	}
}
