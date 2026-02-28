package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.config.configs.GeneralConfig.Craft;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUIngredient;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.NEUKatUpgradeRecipe;
import io.github.moulberry.repo.data.NEURecipe;
import io.github.moulberry.repo.data.NEUTradeRecipe;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftPriceTooltip extends SimpleTooltipAdder {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CraftPriceTooltip.class.getName());
	private static final Map<String, Double> cachedCraftCosts = new ConcurrentHashMap<>();
	private static final int MAX_RECURSION_DEPTH = 15;

	public CraftPriceTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSloFt, ItemStack stack, List<Component> lines) {
		// Don't show crafting cost for non-transferable items while in the rift
		if (Utils.isInTheRift() && ItemUtils.getLoreLineIf(stack, line -> line.contains("Rift-Transferable")) == null) {
			return;
		}

		if (TooltipInfoType.LOWEST_BINS.getData() == null || TooltipInfoType.BAZAAR.getData() == null) {
			ItemTooltip.nullWarning();
			return;
		}

		NEUItem neuItem = NEURepoManager.getItemByNeuId(stack.getNeuName());
		if (neuItem == null) return;

		List<NEURecipe> neuRecipes = neuItem.getRecipes();
		if (neuRecipes.isEmpty()) return;
		NEURecipe recipe = neuRecipes.getFirst();

		try {
			double totalCraftCost = getItemCost(recipe, 0);
			if (totalCraftCost <= 0) return;
			int count = Math.max(ItemUtils.getItemCountInSack(stack, stack.skyblocker$getLoreStrings()).orElse(ItemUtils.getItemCountInStash(lines.getFirst()).orElse(stack.getCount())), 1);

			recipe.getAllOutputs().stream().findFirst().ifPresent(outputIngredient ->
					lines.add(Component.literal(String.format("%-20s", "Crafting Price:")).withStyle(ChatFormatting.GOLD)
								.append(ItemTooltip.getCoinsMessage(totalCraftCost / outputIngredient.getAmount(), count))));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Craft Price] Error calculating craftprice tooltip for: {}", stack.getNeuName(), e);
		}
	}

	public static double getItemCost(NEURecipe recipe, int depth) {
		if (depth >= MAX_RECURSION_DEPTH || recipe instanceof NEUKatUpgradeRecipe || recipe instanceof NEUTradeRecipe) return -1;

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

			NEUItem neuItem = NEURepoManager.getItemByNeuId(inputItemName);
			if (neuItem != null) {
				List<NEURecipe> neuRecipes = neuItem.getRecipes();
				if (!neuRecipes.isEmpty()) {
					double craftCost = getItemCost(neuRecipes.getFirst(), depth + 1);
					if (craftCost != -1) itemCost = Math.min(itemCost, craftCost);
					cachedCraftCosts.put(inputItemName, itemCost);
				}
			}

			totalCraftCost += itemCost * inputItemCount;
		}
		return totalCraftCost;
	}

	public static void clearCache() {
		cachedCraftCosts.clear();
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost != GeneralConfig.Craft.OFF;
	}
}
