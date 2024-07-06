package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUIngredient;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.NEURecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CraftPriceTooltip extends TooltipAdder {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CraftPriceTooltip.class.getName());
    private static final Map<String, Double> cachedCraftCosts = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 15;

    public CraftPriceTooltip(int priority) {
        super(priority);
    }

    @Override
    public void addToTooltip(@Nullable Slot focusedSloFt, ItemStack stack, List<Text> lines) {
        if (SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost == GeneralConfig.Craft.OFF) return;

        String internalID = stack.getSkyblockId();
        if (stack.getNeuName() == null || internalID == null) return;

        if (TooltipInfoType.LOWEST_BINS.getData() == null || TooltipInfoType.BAZAAR.getData() == null) {
            ItemTooltip.nullWarning();
            return;
        }

        NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(internalID);
        if (neuItem == null) return;

        List<NEURecipe> neuRecipes = neuItem.getRecipes();
        if (neuRecipes.isEmpty() || neuRecipes.getFirst() instanceof io.github.moulberry.repo.data.NEUKatUpgradeRecipe) return;

        try {
            double totalCraftCost = getItemCost(neuRecipes.getFirst(), 0);

            if (totalCraftCost == 0) return;

            int amountInStack;
            if (lines.get(1).getString().endsWith("Sack")) {
                String line = lines.get(3).getSiblings().get(1).getString().replace(",", "");
                amountInStack = NumberUtils.isParsable(line) && !line.equals("0") ? Integer.parseInt(line) : stack.getCount();
            } else amountInStack = stack.getCount();

            neuRecipes.getFirst().getAllOutputs().stream().findFirst().ifPresent(outputIngredient ->
                    lines.add(Text.literal(String.format("%-20s", "Crafting Price:")).formatted(Formatting.GOLD)
                            .append(ItemTooltip.getCoinsMessage(totalCraftCost / outputIngredient.getAmount(), amountInStack))));

        } catch (Exception e) {
            LOGGER.error("[Skyblocker Craft Price] Error calculating craftprice tooltip for: " + internalID, e);
        }
    }

    private double getItemCost(NEURecipe recipe, int depth) {
        if (depth >= MAX_RECURSION_DEPTH) return -1;

        double totalCraftCost = 0;
        for (NEUIngredient input : recipe.getAllInputs()) {
            String inputItemName = input.getItemId();
            double inputItemCount = input.getAmount();
            if (cachedCraftCosts.containsKey(inputItemName)) {
                totalCraftCost += cachedCraftCosts.get(inputItemName) * inputItemCount;
                continue;
            }

            double itemCost = 0;

            if (TooltipInfoType.BAZAAR.getData().has(inputItemName)) {
                itemCost = TooltipInfoType.BAZAAR.getData().getAsJsonObject(inputItemName).get(SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost.getOrder()).getAsDouble();
            } else if (TooltipInfoType.LOWEST_BINS.getData().has(inputItemName)) {
                itemCost = TooltipInfoType.LOWEST_BINS.getData().get(inputItemName).getAsDouble();
            }

            if (itemCost > 0) {
                cachedCraftCosts.put(inputItemName, itemCost);
            }

            NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(inputItemName);
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
}
