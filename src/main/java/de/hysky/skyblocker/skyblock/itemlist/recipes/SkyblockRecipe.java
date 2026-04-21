package de.hysky.skyblocker.skyblock.itemlist.recipes;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import io.github.moulberry.repo.data.NEUIngredient;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public interface SkyblockRecipe {
	Logger LOGGER = LoggerFactory.getLogger(SkyblockRecipe.class);
	NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

	static FlexibleItemStack getItemStack(NEUIngredient input) {
		if (input == NEUIngredient.SENTINEL_EMPTY) return FlexibleItemStack.EMPTY;

		String id = input.getItemId();
		int amount = (int) input.getAmount();
		String cacheKey = RecipeItemStackCache.getCacheKey(id, amount);
		FlexibleItemStack cachedStack = RecipeItemStackCache.CACHE.get(cacheKey);

		// Short-circuit with the cached stack when it exists
		if (cachedStack != null) {
			return cachedStack;
		}

		FlexibleItemStack baseStack = ItemRepository.getItemStack(id);
		FlexibleItemStack computedStack = null;

		if (baseStack != null) {
			// If the amount of the ingredient matches the base stack then use that
			if (amount == baseStack.count()) {
				computedStack = baseStack;
			} else {
				// Copy the base stack with the correct amount
				computedStack = baseStack.copyWithCount(amount);
				if (amount > 1) {
					computedStack.set(DataComponents.MAX_STACK_SIZE, amount);
				}
			}
		} else if (id.equals("SKYBLOCK_COIN")) {
			computedStack = new FlexibleItemStack(Items.GOLD_NUGGET);
			computedStack.set(DataComponents.ITEM_NAME, Component.literal("Skyblock Coins").withStyle(ChatFormatting.GOLD));
			computedStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			String format = NUMBER_FORMAT.format(amount);
			computedStack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(format).withStyle(ChatFormatting.GOLD).withStyle(style -> style.withItalic(false)).append(Component.literal(" coins")))));
		} else {
			// Create a fallback stack, cache it, and return it if nothing else worked
			LOGGER.warn("[Skyblocker Recipe] Unable to find item {}", id);
			computedStack = new FlexibleItemStack(Items.BARRIER);
			computedStack.set(DataComponents.ITEM_NAME, Component.literal(id));
		}

		// Cache the computed item stack for this ingredient and return it
		RecipeItemStackCache.CACHE.put(cacheKey, computedStack);

		return computedStack;
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

	List<FlexibleItemStack> getInputs();

	List<FlexibleItemStack> getOutputs();

	/**
	 * Render some extra things, i.e an entity
	 *
	 * @param width  available width
	 * @param height available height
	 * @param mouseX mouse x
	 * @param mouseY mouse y
	 */
	default void extractRenderState(GuiGraphicsExtractor graphics, int width, int height, double mouseX, double mouseY) {}

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

	record RecipeSlot(int x, int y, FlexibleItemStack stack, boolean showBackground) {
		public RecipeSlot(int x, int y, FlexibleItemStack stack) {
			this(x, y, stack, true);
		}
	}
}
