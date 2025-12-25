package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SkyblockCraftingRecipe implements SkyblockRecipe {
	public static final ResourceLocation ID = SkyblockerMod.id("skyblock_crafting");

	private final Component craftText;
	private final List<ItemStack> grid = new ArrayList<>(9);
	private final ItemStack result;

	public SkyblockCraftingRecipe(NEUCraftingRecipe neuCraftingRecipe) {
		this.craftText = neuCraftingRecipe.getExtraText() != null ? Component.literal(neuCraftingRecipe.getExtraText()) : Component.empty();
		for (NEUIngredient input : neuCraftingRecipe.getInputs()) {
			grid.add(SkyblockRecipe.getItemStack(input));
		}
		result = SkyblockRecipe.getItemStack(neuCraftingRecipe.getOutput());
	}

	public List<ItemStack> getGrid() {
		return grid;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public List<RecipeSlot> getInputSlots(int width, int height) {
		ScreenPosition start = new ScreenPosition(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 27 : 32));
		List<RecipeSlot> toReturn = new ArrayList<>(9);
		for (int i = 0; i < grid.size(); i++) {
			int x = i % 3;
			int y = i / 3;
			toReturn.add(new RecipeSlot(start.x() + 1 + x * 18, start.y() + 1 + y * 18, grid.get(i)));
		}
		return toReturn;
	}

	@Override
	public List<RecipeSlot> getOutputSlots(int width, int height) {
		ScreenPosition start = new ScreenPosition(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 26 : 31));
		return List.of(new RecipeSlot(start.x() + 95, start.y() + 19, result));
	}

	@Override
	public List<ItemStack> getInputs() {
		return grid;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return List.of(result);
	}

	@Override
	public Component getExtraText() {
		return craftText;
	}

	@Override
	public ResourceLocation getCategoryIdentifier() {
		return SkyblockCraftingRecipe.ID;
	}

	@Override
	public ResourceLocation getRecipeIdentifier() {
		return ResourceLocation.fromNamespaceAndPath("skyblock", getResult().getSkyblockId().toLowerCase(Locale.ENGLISH).replace(';', '_') + "_" + getResult().getCount());

	}

	@Override
	public @Nullable ScreenPosition getArrowLocation(int width, int height) {
		ScreenPosition start = new ScreenPosition(width / 2 - 58, height / 2 - (getExtraText().getString().isEmpty() ? 26 : 31));
		return new ScreenPosition(start.x() + 60, start.y() + 18);
	}
}
