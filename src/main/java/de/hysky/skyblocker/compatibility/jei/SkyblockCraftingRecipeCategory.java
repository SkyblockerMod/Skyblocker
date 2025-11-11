package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.text.Text;

public final class SkyblockCraftingRecipeCategory extends AbstractSkyblockRecipeCategory<SkyblockCraftingRecipe> {
	private static final IRecipeType<SkyblockCraftingRecipe> RECIPE_TYPE = IRecipeType.create(SkyblockCraftingRecipe.ID, SkyblockCraftingRecipe.class);
	private static final Text TITLE = Text.translatable("emi.category.skyblocker.skyblock_crafting");

	protected SkyblockCraftingRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, RECIPE_TYPE, TITLE, ItemUtils.getSkyblockerStack());
	}
}
