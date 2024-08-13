package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitsTooltip extends SimpleContainerSolver implements TooltipAdder {
    private static final Pattern BITS_PATTERN = Pattern.compile("Cost (?<amount>[\\d,]+) Bits");
    private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Bits");

    public static final BitsTooltip INSTANCE = new BitsTooltip();


    public BitsTooltip() {
        super(".*Community Shop.*");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfigManager.get().general.itemTooltip.showBitsCost;
    }

    double getPrice(ItemStack stack) {
        double itemCost = 0;
        String itemName = stack.getSkyblockApiId();

        if (TooltipInfoType.BAZAAR.getData().has(itemName)) {
            itemCost = TooltipInfoType.BAZAAR.getData().getAsJsonObject(stack.getSkyblockApiId()).get("buyPrice").getAsDouble();
        } else if (TooltipInfoType.LOWEST_BINS.getData().has(itemName)) {
            itemCost = TooltipInfoType.LOWEST_BINS.getData().get(stack.getSkyblockApiId()).getAsDouble();
        }

        return itemCost;
    }

    @Override
    public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
        List<ColorHighlight> highlights = new ArrayList<>();
        double bestCoinsPerBit = 0;
        int bestSlotIndex = -1;

        for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
            ItemStack stack = entry.getValue();
            if (stack == null || stack.isEmpty()) continue;

            String lore = ItemUtils.concatenateLore(ItemUtils.getLore(stack));
            Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
            if (!bitsMatcher.find()) continue;

            long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));

            double itemCost = getPrice(stack);
            if (itemCost == 0) continue;

            long coinsPerBit = Math.round(itemCost / bitsCost);
            LOGGER.info("Coins per bit for {}: {}", stack.getSkyblockApiId(), coinsPerBit);

            if (coinsPerBit > bestCoinsPerBit) {
                bestCoinsPerBit = coinsPerBit;
                bestSlotIndex = entry.getIntKey();
            }

        }

        if (bestSlotIndex != -1) {
            highlights.add(ColorHighlight.green(bestSlotIndex));
        }

        return highlights;
    }

    @Override
    public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
        if (focusedSlot == null) return;

        String lore = ItemUtils.concatenateLore(lines);
        Matcher bitsMatcher = BITS_PATTERN.matcher(lore);
        if (!bitsMatcher.find()) {
            LOGGER.info("No bits pattern found in lore for item: {}", stack.getSkyblockApiId());
            return;
        }

        long bitsCost = Long.parseLong(bitsMatcher.group("amount").replace(",", ""));

        double itemCost = getPrice(stack);
        if (itemCost == 0) {
            LOGGER.info("Item cost is zero for {}", stack.getSkyblockApiId());
            return;
        }

        long coinsPerBit = Math.round(itemCost / bitsCost);
        LOGGER.info("Coins per bit for {}: {}", stack.getSkyblockApiId(), coinsPerBit);

        lines.add(Text.empty()
                .append(Text.literal("Bits Cost: ").formatted(Formatting.AQUA))
                .append(Text.literal(DECIMAL_FORMAT.format(coinsPerBit) + " coins per bit").formatted(Formatting.DARK_AQUA))
        );
    }

    @Override
    public int getPriority() {
        return 0; // Intended to show first
    }
}
