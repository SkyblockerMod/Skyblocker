package de.hysky.skyblocker.compatibility.jei;

import java.util.List;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.text.Text;

public class SkyblockCraftingRecipeCategory extends AbstractRecipeCategory<ShapedRecipe> {
	private static final IRecipeType<ShapedRecipe> SKYBLOCK_RECIPE = IRecipeType.create(SkyblockerMod.id("skyblock"), ShapedRecipe.class);
	private static final Text TITLE = Text.translatable("emi.category.skyblocker.skyblock_crafting");
	private final ICraftingGridHelper craftingGridHelper;

	public SkyblockCraftingRecipeCategory(IGuiHelper guiHelper) {
		super(SKYBLOCK_RECIPE, TITLE, guiHelper.createDrawableItemStack(ItemUtils.getSkyblockerStack()), CraftingRecipeCategory.width, CraftingRecipeCategory.height);
		this.craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayoutBuilder, ShapedRecipe recipe, IFocusGroup focusGroup) {
		ShapedCraftingRecipeDisplay display = (ShapedCraftingRecipeDisplay) recipe.getDisplays().getFirst();
		SlotDisplay resultItem = display.result();
		this.craftingGridHelper.createAndSetOutputs(recipeLayoutBuilder, resultItem);

		List<SlotDisplay> ingredients = display.ingredients();
		int width = recipe.getWidth();
		int height = recipe.getHeight();
		this.craftingGridHelper.createAndSetIngredientsFromDisplays(recipeLayoutBuilder, ingredients, width, height);
	}
}
