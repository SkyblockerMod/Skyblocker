package de.hysky.skyblocker.compatibility.jei;

import java.util.List;

import org.joml.Vector2f;

import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// Note that you must register your own custom recipe type with JEI, you cannot use any types/recipe classes from Vanilla or it will not work!
public abstract sealed class AbstractSkyblockRecipeCategory<T extends SkyblockRecipe> extends AbstractRecipeCategory<T> permits SkyblockCraftingRecipeCategory, SkyblockForgeRecipeCategory, SkyblockNpcShopRecipeCategory {
	protected final IGuiHelper guiHelper;
	protected final ICraftingGridHelper craftingGridHelper;

	protected AbstractSkyblockRecipeCategory(IGuiHelper guiHelper, IRecipeType<T> recipeType, Text title, ItemStack icon) {
		super(recipeType, title, guiHelper.createDrawableItemStack(icon), CraftingRecipeCategory.width, CraftingRecipeCategory.height);
		this.guiHelper = guiHelper;
		this.craftingGridHelper = guiHelper.createCraftingGridHelper();
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, DrawContext context, double mouseX, double mouseY) {
		IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
		recipeArrow.draw(context, 61, (this.getHeight() - recipeArrow.getHeight()) / 2);
	}

	protected void drawTooltip(DrawContext context, Text text, double mouseX, double mouseY) {
		// Tooltip drawing is deferred so we need the current position (since it doesn't save the matrix) and we cannot draw it immediately
		// because things will not layer correctly.
		Vector2f transformedPosition = context.getMatrices().transformPosition((int) mouseX, (int) mouseY, new Vector2f());
		context.drawTooltip(text, (int) transformedPosition.x(), (int) transformedPosition.y());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		// All of the SkyblockRecipe implementations only have a singular output
		SlotDisplay result = new SlotDisplay.StackSlotDisplay(recipe.getOutputs().getFirst());
		List<SlotDisplay> ingredients = recipe.getInputs().stream()
				.map(SlotDisplay.StackSlotDisplay::new)
				.map(SlotDisplay.class::cast)
				.toList();

		// Modified from ICraftingCategoryExtension#setRecipe
		// Note: JEI only uses the width and height to make sure they aren't zero, so the grid is always 3x3.
		this.craftingGridHelper.createAndSetOutputs(builder, result);
		this.craftingGridHelper.createAndSetIngredientsFromDisplays(builder, ingredients, this.getCraftingGridLayoutSize(), this.getCraftingGridLayoutSize());
	}

	/**
	 * Allows you to control which slots items are inserted into, however JEI will always render the grid as 3x3.
	 */
	protected int getCraftingGridLayoutSize() {
		return 3;
	}

	@Override
	public Identifier getRegistryName(T recipe) {
		return recipe.getRecipeIdentifier();
	}
}
