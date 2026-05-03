package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.SkyblockTime;
import io.github.moulberry.repo.data.NEUIngredient;
import io.github.moulberry.repo.data.NEUKatUpgradeRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SkyblockKatUpgradeRecipe implements CenteredRecipe {
	public static final Identifier ID = SkyblockerMod.id("skyblock_kat_upgrade");
	public static @Nullable FlexibleItemStack katIcon = null;

	private final FlexibleItemStack basePet;
	private final List<FlexibleItemStack> inputs;
	private final FlexibleItemStack upgradedPet;

	private final Component upgradeTime;

	public SkyblockKatUpgradeRecipe(NEUKatUpgradeRecipe recipe) {
		if (katIcon == null) katIcon = ItemRepository.getItemStack("KAT_NPC");
		upgradeTime = SkyblockTime.formatTime(recipe.getSeconds());
		basePet = SkyblockRecipe.getItemStack(recipe.getInput());
		upgradedPet = SkyblockRecipe.getItemStack(recipe.getOutput());

		inputs = new ArrayList<>();
		inputs.add(basePet);
		recipe.getItems().stream().map(SkyblockRecipe::getItemStack).forEach(inputs::add);
		inputs.add(SkyblockRecipe.getItemStack(NEUIngredient.ofCoins(recipe.getCoins())));
	}

	@Override
	public List<RecipeSlot> getInputSlots(int width, int height) {
		return CenteredRecipe.arrangeInputs(width, height, null, inputs);
	}

	@Override
	public List<RecipeSlot> getOutputSlots(int width, int height) {
		return CenteredRecipe.arrangeOutputs(width, height, true, inputs.size(), upgradedPet);
	}

	@Override
	public @Nullable ScreenPosition getArrowLocation(int width, int height) {
		return CenteredRecipe.getArrowLocation(width, height, true, inputs.size());
	}

	@Override
	public List<FlexibleItemStack> getInputs() {
		return inputs;
	}

	@Override
	public List<FlexibleItemStack> getOutputs() {
		return List.of(upgradedPet);
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
		return Identifier.fromNamespaceAndPath("skyblock", upgradedPet.getSkyblockId().toLowerCase(Locale.ENGLISH).replace(';', '_'));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int width, int height, double mouseX, double mouseY) {
		ScreenPosition arrowLocation = getArrowLocation(width, height);
		if (arrowLocation == null) return;
		graphics.centeredText(Minecraft.getInstance().font, upgradeTime, arrowLocation.x() + 12, arrowLocation.y() - 10, CommonColors.WHITE);
	}

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.BONE;
	}

	@Override
	public @Nullable FlexibleItemStack getRepresentative() {
		return katIcon;
	}
}
