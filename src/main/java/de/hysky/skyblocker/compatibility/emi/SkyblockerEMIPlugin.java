package de.hysky.skyblocker.compatibility.emi;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import java.util.Map;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * EMI integration
 */
public class SkyblockerEMIPlugin implements EmiPlugin {
	public static final Identifier SIMPLIFIED_TEXTURES = SkyblockerMod.id("textures/gui/emi_icons.png");

	public static final EmiRecipeCategory SKYBLOCK_CRAFTING = new EmiRecipeCategory(SkyblockCraftingRecipe.ID, EmiStack.of(Items.CRAFTING_TABLE), new EmiTexture(SIMPLIFIED_TEXTURES, 0, 0, 16, 16));
	public static final EmiRecipeCategory SKYBLOCK_FORGE = new EmiRecipeCategory(SkyblockForgeRecipe.ID, EmiStack.of(Items.LAVA_BUCKET), new EmiTexture(SIMPLIFIED_TEXTURES, 16, 0, 16, 16));

	protected static final Map<Identifier, EmiRecipeCategory> IDENTIFIER_CATEGORY_MAP = Map.of(
			SkyblockCraftingRecipe.ID, SKYBLOCK_CRAFTING,
			SkyblockForgeRecipe.ID, SKYBLOCK_FORGE
	);

	@Override
	public void register(EmiRegistry registry) {
		if (!SkyblockerConfigManager.get().general.itemList.enableItemList) return;
		ItemRepository.getItemsStream().map(EmiStack::of).forEach(emiStack -> {
			registry.addEmiStack(emiStack);
			registry.setDefaultComparison(emiStack, Comparison.compareData(emiStack1 -> emiStack1.getItemStack().getSkyblockId()));
		});
		registry.addCategory(SKYBLOCK_CRAFTING);
		registry.addCategory(SKYBLOCK_FORGE);
		registry.addWorkstation(SKYBLOCK_CRAFTING, EmiStack.of(Items.CRAFTING_TABLE));
		registry.addWorkstation(SKYBLOCK_CRAFTING, EmiStack.of(Items.LAVA_BUCKET));
		ItemRepository.getRecipesStream().map(SkyblockEmiRecipe::new).forEach(registry::addRecipe);
		registry.addExclusionArea(InventoryScreen.class, (screen, consumer) -> {
			if (!SkyblockerConfigManager.get().farming.garden.gardenPlotsWidget || !Utils.getLocation().equals(Location.GARDEN)) return;
			HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
			consumer.accept(new Bounds(accessor.getX() + accessor.getImageWidth() + 4, accessor.getY(), 104, 127));
		});
	}
}
