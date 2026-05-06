package de.hysky.skyblocker.skyblock.itemlist.recipes;

import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface CenteredRecipe extends SkyblockRecipe {
	int SLOT_SIZE = 18;
	int ARROW_LENGTH = 24;
	int ARROW_PADDING = 3;

	private static boolean shouldSplit(int size) {
		return size > 3;
	}

	private static int getRowSize(int size) {
		return shouldSplit(size) ? Math.floorDiv(size, 2) : size;
	}

	/**
	 * For larger recipes, we shift the center slightly so all the items fit on the screen.
	 * <p>
	 * Recipes greater than 3 items are split into 2 rows evenly.
	 * If the input size is odd, it is offset further so those items do not overlap with the arrow.
	 */
	private static int getCenterX(int width, int size) {
		int centerX = width / 2;
		centerX += Math.min(getRowSize(size), 3) * SLOT_SIZE / 2 - SLOT_SIZE / 2;
		if (size > 1 && shouldOffsetArrow(size)) centerX -= SLOT_SIZE / 2;
		return centerX;
	}

	private static int getCenterY(int height, boolean shouldOffset) {
		if (shouldOffset) height -= SLOT_SIZE;
		return height / 2;
	}

	/**
	 * Input items are displayed in 1 or 2 rows depending on the recipe size.
	 */
	static List<SkyblockRecipe.RecipeSlot> arrangeInputs(int width, int height, @Nullable ItemStack centeredItem, List<ItemStack> inputs) {
		List<SkyblockRecipe.RecipeSlot> slots = new ArrayList<>();
		if (centeredItem != null)
			slots.add(new SkyblockRecipe.RecipeSlot((width - SLOT_SIZE) / 2, SLOT_SIZE / 2, centeredItem));


		int size = inputs.size();
		int centerX = getCenterX(width, size);
		int centerY = getCenterY(height, centeredItem == null);

		boolean onSecondRow = false; // Max of 2 rows
		int rowSize = getRowSize(size);

		int x = centerX - (SLOT_SIZE * Math.min(rowSize, 3)) - ARROW_LENGTH / 2 - ARROW_PADDING;
		int y = shouldSplit(size) ? centerY - SLOT_SIZE / 2 + 3 : centerY;

		for (int i = 0; i < size; i++) {
			slots.add(new SkyblockRecipe.RecipeSlot(x, y, inputs.get(i)));
			x += SLOT_SIZE;
			if (((i + 1) % rowSize == 0) && !onSecondRow) {
				onSecondRow = true;
				x -= rowSize * SLOT_SIZE;
				y += SLOT_SIZE;
			}
		}

		return slots;
	}

	static boolean shouldOffsetArrow(int size) {
		return shouldSplit(size) && (size % 2 == 1 || size >= 8);
	}

	static List<SkyblockRecipe.RecipeSlot> arrangeOutputs(int width, int height, boolean offsetY, int inputSize, ItemStack output) {
		int centerX = getCenterX(width, inputSize);
		int centerY = getCenterY(height, offsetY);
		if (shouldOffsetArrow(inputSize)) centerX += SLOT_SIZE;
		if (shouldSplit(inputSize)) centerY += 2;
		return List.of(new SkyblockRecipe.RecipeSlot(centerX + ARROW_LENGTH / 2 + ARROW_PADDING, centerY, output));
	}

	static ScreenPosition getArrowLocation(int width, int height, boolean offsetY, int inputSize) {
		int centerX = getCenterX(width, inputSize);
		int centerY = getCenterY(height, offsetY);
		if (shouldOffsetArrow(inputSize)) centerX += SLOT_SIZE;
		if (shouldSplit(inputSize)) centerY += 3;
		return new ScreenPosition(centerX - ARROW_LENGTH / 2 - 1, centerY - 1);
	}

	ItemStack getIcon();

	@Nullable ItemStack getRepresentative();
}
