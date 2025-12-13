package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import io.github.moulberry.repo.data.NEUNpcShopRecipe;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

public class SkyblockNpcShopRecipe implements SkyblockRecipe {
	public static final Identifier ID = SkyblockerMod.id("skyblock_npc_shop");
	private static final int SLOT_SIZE = 18;
	private static final int ARROW_LENGTH = 24;
	private static final int ARROW_PADDING = 3;

	private final ItemStack npcShop;
	private final List<ItemStack> inputs;
	private final ItemStack output;

	public SkyblockNpcShopRecipe(NEUNpcShopRecipe shopRecipe) {
		npcShop = ItemRepository.getItemStack(shopRecipe.getIsSoldBy().getSkyblockItemId());
		inputs = shopRecipe.getCost().stream().map(SkyblockRecipe::getItemStack).toList();
		output = SkyblockRecipe.getItemStack(shopRecipe.getResult());
	}

	private boolean shouldSplit() {
		return inputs.size() > 3;
	}

	private int getRowSize() {
		return shouldSplit() ? Math.floorDiv(inputs.size(), 2) : inputs.size();
	}

	/**
	 * For larger recipes, we shift the center slightly so all the items fit on the screen.
	 * <p>
	 * Recipes greater than 3 items are split into 2 rows evenly.
	 * If the input size is odd, it is offset further so those items do not overlap with the arrow.
	 * There are currently no recipes with > 7 items.
	 */
	private int getCenterX(int width) {
		int centerX = width / 2;
		int size = inputs.size();
		centerX += Math.min(getRowSize(), 3) * SLOT_SIZE / 2 - SLOT_SIZE / 2;
		if (size > 1 && size % 2 == 1) centerX -= SLOT_SIZE / 2;
		return centerX;
	}

	/**
	 * Input items are displayed in 1 or 2 rows depending on the recipe size.
	 */
	@Override
	public List<RecipeSlot> getInputSlots(int width, int height) {
		List<RecipeSlot> slots = new ArrayList<>();
		slots.add(new RecipeSlot((width - SLOT_SIZE) / 2, SLOT_SIZE / 2, npcShop));

		int centerX = getCenterX(width);
		int centerY = height / 2;

		boolean onSecondRow = false; // Max of 2 rows
		int rowSize = getRowSize();

		int x = centerX - (SLOT_SIZE * Math.min(rowSize, 3)) - ARROW_LENGTH / 2 - ARROW_PADDING;
		int y = shouldSplit() ? centerY - SLOT_SIZE / 2 + 3 : centerY;

		for (int i = 0; i < inputs.size(); i++) {
			slots.add(new RecipeSlot(x, y, inputs.get(i)));
			x += SLOT_SIZE;
			if (((i + 1) % rowSize == 0) && !onSecondRow) {
				onSecondRow = true;
				x -= rowSize * SLOT_SIZE;
				y += SLOT_SIZE;
			}
		}

		return slots;
	}

	@Override
	public List<RecipeSlot> getOutputSlots(int width, int height) {
		int centerX = getCenterX(width);
		int centerY = height / 2;
		if (inputs.size() == 7 || inputs.size() == 8) centerX += SLOT_SIZE;
		return List.of(new RecipeSlot(centerX + ARROW_LENGTH / 2 + ARROW_PADDING, centerY, output));
	}

	@Override
	public @Nullable ScreenPos getArrowLocation(int width, int height) {
		int centerX = getCenterX(width);
		int centerY = height / 2;
		if (inputs.size() == 7 || inputs.size() == 8) centerX += SLOT_SIZE;
		return new ScreenPos(centerX - ARROW_LENGTH / 2 - 1, centerY);
	}

	public ItemStack getNpcItem() {
		return npcShop;
	}

	@Override
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(output);
	}

	@Override
	public Text getExtraText() {
		return Text.empty();
	}

	@Override
	public Identifier getCategoryIdentifier() {
		return ID;
	}

	@Override
	public Identifier getRecipeIdentifier() {
		return Identifier.of("skyblock", output.getSkyblockId().toLowerCase(Locale.ENGLISH).replace(';', '_') + "_" + output.getCount());
	}
}
