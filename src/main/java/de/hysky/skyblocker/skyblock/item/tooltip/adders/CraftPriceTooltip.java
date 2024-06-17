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

public class CraftPriceTooltip extends TooltipAdder {
    public CraftPriceTooltip(int priority) {
        super(priority);
    }

    @Override
    public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        if (!SkyblockerConfigManager.get().general.itemTooltip.enableCraftingCost) return;

        String neuName = stack.getNeuName();
        String internalID = stack.getSkyblockId();
        if (neuName == null || internalID == null) return;

        if (TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.BAZAAR.getData() == null) {
            ItemTooltip.nullWarning();
            return;
        }

        NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(internalID);
        if (neuItem == null) return;

        List<NEURecipe> neuRecipes = neuItem.getRecipes();
        if (neuRecipes.isEmpty()) return;

        double totalCraftCost = 0.0;

        for (NEUIngredient input : neuRecipes.getFirst().getAllInputs()) {
            String inputItemName = input.getItemId();
            double inputItemCount = input.getAmount();

            double inputItemCost = getItemCost(inputItemName);
            totalCraftCost += inputItemCost * inputItemCount;
        }

        if (totalCraftCost == 0) return;

        lines.add(Text.literal(String.format("%-18s", "Crafting Cost:"))
                .formatted(Formatting.GOLD)
                .append(ItemTooltip.getCoinsMessage(totalCraftCost, 1)));
    }

    private double getItemCost(String itemName) {
        if (TooltipInfoType.BAZAAR.getData().has(itemName)) {
            return TooltipInfoType.BAZAAR.getData().getAsJsonObject(itemName).get("sellPrice").getAsDouble();
        } else if (TooltipInfoType.LOWEST_BINS.getData().has(itemName)) {
            return TooltipInfoType.LOWEST_BINS.getData().get(itemName).getAsDouble();
        } else {
            return 0.0;  // No data available
        }
    }
}
