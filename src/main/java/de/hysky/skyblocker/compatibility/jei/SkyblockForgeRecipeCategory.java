package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.HudHelper;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SkyblockForgeRecipeCategory extends AbstractSkyblockRecipeCategory<SkyblockForgeRecipe> {
	private static final IRecipeType<SkyblockForgeRecipe> RECIPE_TYPE = IRecipeType.create(SkyblockForgeRecipe.ID, SkyblockForgeRecipe.class);
	private static final Text TITLE = Text.translatable("emi.category.skyblocker.skyblock_forge");

	protected SkyblockForgeRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, RECIPE_TYPE, TITLE, ItemUtils.getSkyblockerForgeStack());
	}

	@Override
	public void draw(SkyblockForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, DrawContext context, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
		int arrowYOffset = (this.getHeight() - recipeArrow.getHeight()) / 2;
		recipeArrow.draw(context, 61, arrowYOffset);

		if (HudHelper.pointIsInArea(mouseX, mouseY, 61, arrowYOffset, 61 + recipeArrow.getWidth(), arrowYOffset + recipeArrow.getHeight())) {
			this.drawTooltip(context, Text.literal(recipe.getDurationString()), mouseX, mouseY);
		}
	}

	@Override
	protected int getCraftingGridLayoutSize() {
		return 2;
	}
}
