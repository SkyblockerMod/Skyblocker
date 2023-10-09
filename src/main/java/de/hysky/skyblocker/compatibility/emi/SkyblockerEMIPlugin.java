package de.hysky.skyblocker.compatibility.emi;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRegistry;
import de.hysky.skyblocker.utils.ItemUtils;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * EMI integration
 */
public class SkyblockerEMIPlugin implements EmiPlugin {
    public static final Identifier SIMPLIFIED_TEXTURES = new Identifier("emi", "textures/gui/widgets.png");
    // TODO: Custom simplified texture for Skyblock
    public static final EmiRecipeCategory SKYBLOCK = new EmiRecipeCategory(new Identifier(SkyblockerMod.NAMESPACE, "skyblock"), EmiStack.of(ItemUtils.getSkyblockerStack()), new EmiTexture(SIMPLIFIED_TEXTURES, 240, 240, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        ItemRegistry.getItemsStream().map(EmiStack::of).forEach(registry::addEmiStack);
        registry.addCategory(SKYBLOCK);
        registry.addWorkstation(SKYBLOCK, EmiStack.of(Items.CRAFTING_TABLE));
        ItemRegistry.getRecipesStream().map(SkyblockEmiRecipe::new).forEach(registry::addRecipe);
    }
}
