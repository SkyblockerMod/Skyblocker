package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.config.configs.GeneralConfig.Craft;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUIngredient;
import io.github.moulberry.repo.data.NEUKatUpgradeRecipe;
import io.github.moulberry.repo.data.NEUMobDropRecipe;
import io.github.moulberry.repo.data.NEURecipe;
import io.github.moulberry.repo.data.NEUTradeRecipe;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftPriceTooltip extends SimpleTooltipAdder {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CraftPriceTooltip.class.getName());
	private static final Map<String, Double> cachedCraftCosts = Object2DoubleMaps.synchronize(new Object2DoubleOpenHashMap<>());
	private static final int MAX_RECURSION_DEPTH = 15;

	public CraftPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (TooltipInfoType.LOWEST_BINS.getData() == null || TooltipInfoType.BAZAAR.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}

		String itemId = stack.getNeuName();
		try {
			double craftPrice = cachedCraftCosts.computeIfAbsent(itemId, CraftPriceTooltip::getItemCost);
			if (craftPrice <= 0) return;
			int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreStrings()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(stack.getCount())), 1);
			lines.add(Component.literal(String.format("%-20s", "Crafting Price:")).withStyle(ChatFormatting.GOLD)
						.append(ItemTooltip.getCoinsMessage(craftPrice, count)));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Craft Price] Error calculating craft price for: {}", stack.getNeuName(), e);
		}
	}

	public static double getItemCost(String neuId) {
		return getItemCost(neuId, 0);
	}

	public static double getItemCost(String neuId, int depth) {
		Set<NEURecipe> neuRecipes = NEURepoManager.getRecipes().get(neuId);
		if (neuRecipes == null || neuRecipes.isEmpty()) return -1;

		Optional<NEURecipe> recipe = neuRecipes.stream().filter(CraftPriceTooltip::isValidRecipe).findFirst();
		if (recipe.isEmpty()) return -1;
		return recipe.get().getAllOutputs().stream().filter(x -> x.getItemId().equals(neuId)).map(NEUIngredient::getAmount).findAny()
				.map(amount -> getRecipeCost(recipe.get(), depth + 1) / amount).orElse(-1.0);
	}

	public static double getRecipeCost(NEURecipe recipe, int depth) {
		if (depth >= MAX_RECURSION_DEPTH || !isValidRecipe(recipe)) return -1;

		double totalCraftCost = 0;
		for (NEUIngredient input : recipe.getAllInputs()) {
			String inputItemName = input.getItemId();
			double inputItemCount = input.getAmount();
			if (cachedCraftCosts.containsKey(inputItemName)) {
				totalCraftCost += cachedCraftCosts.get(inputItemName) * inputItemCount;
				continue;
			}

			double itemCost = 0;

			Object2ObjectMap<String, BazaarProduct> bazaarData = TooltipInfoType.BAZAAR.getData();
			Object2DoubleMap<String> lowestBinsData = TooltipInfoType.LOWEST_BINS.getData();
			if (bazaarData != null && bazaarData.containsKey(inputItemName)) {
				BazaarProduct product = bazaarData.get(inputItemName);
				itemCost = SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost == Craft.BUY_ORDER ? product.buyPrice().orElse(0d) : product.sellPrice().orElse(0d);
			} else if (lowestBinsData != null && lowestBinsData.containsKey(inputItemName)) {
				itemCost = lowestBinsData.getDouble(inputItemName);
			}

			if (itemCost > 0) {
				cachedCraftCosts.put(inputItemName, itemCost);
			}

			double craftCost = getItemCost(inputItemName, depth + 1);
			if (craftCost > 0) {
				if (itemCost > 0) itemCost = Math.min(itemCost, craftCost);
				else itemCost = craftCost;
				cachedCraftCosts.put(inputItemName, itemCost);
			}

			totalCraftCost += itemCost * inputItemCount;
		}
		return totalCraftCost;
	}

	public static boolean isValidRecipe(NEURecipe recipe) {
		return !(recipe instanceof NEUKatUpgradeRecipe || recipe instanceof NEUTradeRecipe || recipe instanceof NEUMobDropRecipe);
	}

	public static void clearCache() {
		cachedCraftCosts.clear();
		cachedCraftCosts.put(NEUIngredient.NEU_SENTINEL_COINS, 1d);
		cachedCraftCosts.put(NEUIngredient.NEU_SENTINEL_EMPTY, 0d);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost != GeneralConfig.Craft.OFF;
	}
}
