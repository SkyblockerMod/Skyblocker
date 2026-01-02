package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import io.github.moulberry.repo.data.NEUIngredient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public interface SkyblockRecipe {
	Logger LOGGER = LoggerFactory.getLogger(SkyblockRecipe.class);
	NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	static ItemStack getItemStack(NEUIngredient input) {
		if (input == NEUIngredient.SENTINEL_EMPTY) return Items.AIR.getDefaultInstance();

		ItemStack stack = ItemRepository.getItemStack(input.getItemId());
		if (stack != null) {
			return stack.copyWithCount((int) input.getAmount());
		} else if (input.getItemId().equals("SKYBLOCK_COIN")) {
			ItemStack itemStack = new ItemStack(Items.GOLD_NUGGET);
			itemStack.set(DataComponents.ITEM_NAME, Component.literal("Skyblock Coins").withStyle(ChatFormatting.GOLD));
			itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			String format = NUMBER_FORMAT.format(input.getAmount());
			itemStack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(format).withStyle(ChatFormatting.GOLD).withStyle(style -> style.withItalic(false)).append(Component.literal(" coins")))));
			return itemStack;
		}

		LOGGER.warn("[Skyblocker Recipe] Unable to find item {}", input.getItemId());
		ItemStack fallbackStack = new ItemStack(Items.BARRIER);
		fallbackStack.set(DataComponents.ITEM_NAME, Component.literal(input.getItemId()));
		return fallbackStack;
	}

	/**
	 * @param width  the available area's width
	 * @param height the available area's height
	 * @return list of slots
	 */
	List<RecipeSlot> getInputSlots(int width, int height);

	/**
	 * @param width  the available area's width
	 * @param height the available area's height
	 * @return list of slots
	 */
	List<RecipeSlot> getOutputSlots(int width, int height);

	default @Nullable ScreenPosition getArrowLocation(int width, int height) {
		return null;
	}

	List<ItemStack> getInputs();

	List<ItemStack> getOutputs();

	/**
	 * Render some extra things, i.e an entity
	 *
	 * @param width  available width
	 * @param height available height
	 * @param mouseX mouse x
	 * @param mouseY mouse y
	 */
	default void render(GuiGraphics context, int width, int height, double mouseX, double mouseY) {}

	/**
	 * Extra text like collection requirements
	 */
	Component getExtraText();

	/**
	 * Identifier used for REI, EMI. Also used in the recipe book for the name
	 */
	Identifier getCategoryIdentifier();

	/**
	 * Used for EMI.
	 */
	Identifier getRecipeIdentifier();

	record RecipeSlot(int x, int y, ItemStack stack, boolean showBackground) {
		public RecipeSlot(int x, int y, ItemStack stack) {
			this(x, y, stack, true);
		}
	}
}
