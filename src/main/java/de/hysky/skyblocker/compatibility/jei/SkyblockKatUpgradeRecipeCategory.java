package de.hysky.skyblocker.compatibility.jei;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockKatUpgradeRecipe;
import de.hysky.skyblocker.utils.render.GuiHelper;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public final class SkyblockKatUpgradeRecipeCategory extends AbstractSkyblockRecipeCategory<SkyblockKatUpgradeRecipe> {
	private static final IRecipeType<SkyblockKatUpgradeRecipe> RECIPE_TYPE = IRecipeType.create(SkyblockKatUpgradeRecipe.ID, SkyblockKatUpgradeRecipe.class);
	private static final Component TITLE = Component.translatable("emi.category.skyblocker.skyblock_kat_upgrade");
	private static final int ITEM_SIZE = 16;
	private static final int NPC_ITEM_PADDING = 2;

	protected SkyblockKatUpgradeRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, RECIPE_TYPE, TITLE, Items.GOLD_NUGGET.getDefaultInstance());
	}

	@Override
	public void draw(SkyblockKatUpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
		int arrowYOffset = (this.getHeight() - recipeArrow.getHeight()) / 2;
		recipeArrow.draw(graphics, 61, arrowYOffset);

		int itemX = 61 + ((recipeArrow.getWidth() - ITEM_SIZE) / 2);
		int itemY = arrowYOffset - ITEM_SIZE - NPC_ITEM_PADDING;
		if (recipe.getRepresentative() == null) return;
		graphics.item(recipe.getRepresentative(), itemX, itemY);

		if (GuiHelper.pointIsInArea(mouseX, mouseY, itemX, itemY, itemX + ITEM_SIZE, itemY + ITEM_SIZE)) {
			this.drawTooltip(graphics, recipe.getRepresentative().getHoverName(), mouseX, mouseY);
		}
	}
}
