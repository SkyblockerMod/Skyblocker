package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CraftPriceTooltip extends TooltipAdder {
    private final Map<String, Double> cachedCraftCosts = new HashMap<>();
    private static final int MAX_RECURSION_DEPTH = 15;

    public CraftPriceTooltip(int priority) {
        super(priority);
    }

    @Override
    public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        if (!SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost) return;

        String neuName = stack.getNeuName();
        String internalID = stack.getSkyblockId();
        if (neuName == null || internalID == null) return;

        if (TooltipInfoType.LOWEST_BINS.getData() == null || TooltipInfoType.BAZAAR.getData() == null) {
            ItemTooltip.nullWarning();
            return;
        }

        NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(internalID);
        if (neuItem == null) return;

        List<NEURecipe> neuRecipes = neuItem.getRecipes();
        if (neuRecipes.isEmpty()) return;

        double totalCraftCost = getItemCost(neuRecipes.getFirst(), 0);

        if (totalCraftCost == 0) return;

        lines.add(Text.literal(String.format("%-18s", "Crafting Cost:"))
                .formatted(Formatting.GOLD)
                .append(ItemTooltip.getCoinsMessage(totalCraftCost, stack.getCount())));
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
                itemCost = TooltipInfoType.BAZAAR.getData().getAsJsonObject(inputItemName).get("sellPrice").getAsDouble();
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
}
