package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import io.github.moulberry.repo.data.NEUNpcShopRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class SkyblockNpcShopRecipe implements CenteredRecipe {
	public static final Identifier ID = SkyblockerMod.id("skyblock_npc_shop");

	private final FlexibleItemStack npcShop;
	private final List<FlexibleItemStack> inputs;
	private final FlexibleItemStack output;

	public SkyblockNpcShopRecipe(NEUNpcShopRecipe shopRecipe) {
		npcShop = ItemRepository.getItemStack(shopRecipe.getIsSoldBy().getSkyblockItemId());
		inputs = shopRecipe.getCost().stream().map(SkyblockRecipe::getItemStack).toList();
		output = SkyblockRecipe.getItemStack(shopRecipe.getResult());
	}

	@Override
	public List<RecipeSlot> getInputSlots(int width, int height) {
		return CenteredRecipe.arrangeInputs(width, height, npcShop, inputs);
	}

	@Override
	public List<RecipeSlot> getOutputSlots(int width, int height) {
		return CenteredRecipe.arrangeOutputs(width, height, false, inputs.size(), output);
	}

	@Override
	public @Nullable ScreenPosition getArrowLocation(int width, int height) {
		return CenteredRecipe.getArrowLocation(width, height, false, inputs.size());
	}

	@Override
	public FlexibleItemStack getRepresentative() {
		return npcShop;
	}

	@Override
	public List<FlexibleItemStack> getInputs() {
		return inputs;
	}

	@Override
	public List<FlexibleItemStack> getOutputs() {
		return Collections.singletonList(output);
	}

	@Override
	public Component getExtraText() {
		return Component.empty();
	}

	@Override
	public Identifier getCategoryIdentifier() {
		return ID;
	}

	@Override
	public Identifier getRecipeIdentifier() {
		return Identifier.fromNamespaceAndPath("skyblock", output.getSkyblockId().toLowerCase(Locale.ENGLISH).replace(';', '_') + "_" + output.count());
	}

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.GOLD_NUGGET;
	}
}
