package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockNpcShopRecipe;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.render.GuiHelper;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class SkyblockNpcShopRecipeCategory extends AbstractSkyblockRecipeCategory<SkyblockNpcShopRecipe> {
	private static final IRecipeType<SkyblockNpcShopRecipe> RECIPE_TYPE = IRecipeType.create(SkyblockNpcShopRecipe.ID, SkyblockNpcShopRecipe.class);
	private static final Component TITLE = Component.translatable("emi.category.skyblocker.skyblock_npc_shop");
	private static final int ITEM_SIZE = 16;
	private static final int NPC_ITEM_PADDING = 2;

	protected SkyblockNpcShopRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, RECIPE_TYPE, TITLE, Ico.GOLD_NUGGET);
	}

	@Override
	public void draw(SkyblockNpcShopRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor context, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
		int arrowYOffset = (this.getHeight() - recipeArrow.getHeight()) / 2;
		recipeArrow.draw(context, 61, arrowYOffset);

		int itemX = 61 + ((recipeArrow.getWidth() - ITEM_SIZE) / 2);
		int itemY = arrowYOffset - ITEM_SIZE - NPC_ITEM_PADDING;
		context.item(recipe.getRepresentative().getStackOrThrow(), itemX, itemY);

		if (GuiHelper.pointIsInArea(mouseX, mouseY, itemX, itemY, itemX + ITEM_SIZE, itemY + ITEM_SIZE)) {
			this.drawTooltip(context, recipe.getRepresentative().getStackOrThrow().getHoverName(), mouseX, mouseY);
		}
	}
}
