package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import io.github.moulberry.repo.data.NEUNpcShopRecipe;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkyblockNpcShopRecipe implements SkyblockRecipe {
	public static final Identifier IDENTIFIER = Identifier.of(SkyblockerMod.NAMESPACE, "skyblock_npc_shop");
	private static final int SLOT_SIZE = 18;
	private static final int ARROW_LENGTH = 24;
	private static final int ARROW_PADDING = 3;

	private final ItemStack npcShop;
	private final List<ItemStack> inputs;
	private final ItemStack output;

	public SkyblockNpcShopRecipe(NEUNpcShopRecipe shopRecipe) {
		npcShop = ItemRepository.getItemStack(shopRecipe.getIsSoldBy().getSkyblockItemId());
		inputs = new ArrayList<>();
		shopRecipe.getCost().stream().map(SkyblockRecipe::getItemStack).forEach(inputs::add);
		output = SkyblockRecipe.getItemStack(shopRecipe.getResult());
	}

	@Override
	public List<RecipeSlot> getInputSlots(int width, int height) {
		List<RecipeSlot> slots = new ArrayList<>();
		int centerX = width / 2;
		int centerY = height / 2;

		slots.add(new RecipeSlot(centerX - SLOT_SIZE / 2, SLOT_SIZE / 2, npcShop));
		if (inputs.size() > 3) centerX += (inputs.size() - 3) * SLOT_SIZE + SLOT_SIZE / 2; // 3+ items

		int x = centerX - (SLOT_SIZE * inputs.size()) - ARROW_LENGTH / 2 - ARROW_PADDING;
		for (ItemStack input : inputs) {
			slots.add(new RecipeSlot(x, centerY, input));
			x += SLOT_SIZE;
		}

		slots.add(new RecipeSlot(centerX + ARROW_LENGTH / 2 + ARROW_PADDING, centerY, output));

		return slots;
	}

	@Override
	public List<RecipeSlot> getOutputSlots(int width, int height) {
		return List.of();
	}

	@Override
	public @Nullable ScreenPos getArrowLocation(int width, int height) {
		int centerX = width / 2;
		if (inputs.size() > 3) centerX += (inputs.size() - 3) * SLOT_SIZE + SLOT_SIZE / 2; // 3+ items
		int centerY = height / 2;
		return new ScreenPos(centerX - ARROW_LENGTH / 2 - 1, centerY);
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
		return IDENTIFIER;
	}

	@Override
	public Identifier getRecipeIdentifier() {
		return Identifier.of("skyblock", ItemUtils.getItemId(output));
	}
}
