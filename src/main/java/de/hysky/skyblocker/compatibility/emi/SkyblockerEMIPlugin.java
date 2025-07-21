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
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * EMI integration
 */
public class SkyblockerEMIPlugin implements EmiPlugin {
    public static final Identifier SIMPLIFIED_TEXTURES = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/emi_icons.png");

    public static final EmiRecipeCategory SKYBLOCK_CRAFTING = new EmiRecipeCategory(SkyblockCraftingRecipe.IDENTIFIER, EmiStack.of(Items.CRAFTING_TABLE), new EmiTexture(SIMPLIFIED_TEXTURES, 0, 0, 16, 16));
    public static final EmiRecipeCategory SKYBLOCK_FORGE = new EmiRecipeCategory(SkyblockForgeRecipe.IDENTIFIER, EmiStack.of(Items.LAVA_BUCKET), new EmiTexture(SIMPLIFIED_TEXTURES, 16, 0, 16, 16));

    protected static final Map<Identifier, EmiRecipeCategory> IDENTIFIER_CATEGORY_MAP = Map.of(
            SkyblockCraftingRecipe.IDENTIFIER, SKYBLOCK_CRAFTING,
            SkyblockForgeRecipe.IDENTIFIER, SKYBLOCK_FORGE
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
            consumer.accept(new Bounds(accessor.getX() + accessor.getBackgroundWidth() + 4, accessor.getY(), 104, 127));
        });
    }
}
