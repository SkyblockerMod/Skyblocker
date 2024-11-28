package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemTooltip {
    public static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final GeneralConfig.ItemTooltip config = SkyblockerConfigManager.get().general.itemTooltip;
    private static volatile boolean sentNullWarning = false;

    /**
     * @deprecated Use {@link ItemUtils#getNeuId(ItemStack)} instead
     */
    @Deprecated(since = "1.22.0")
    public static String getNeuName(String id, String apiId) {
        LOGGER.error("[Skyblocker Item Tooltip] ItemTooltip.getNeuName is deprecated and will not work. Use ItemStack#getNeuName instead.");
        return "";
    }

    public static void nullWarning() {
        if (!sentNullWarning && client.player != null) {
            LOGGER.warn(Constants.PREFIX.get().append(Text.translatable("skyblocker.itemTooltip.nullMessage")).getString());
            sentNullWarning = true;
        }
    }

    public static Text getCoinsMessage(double price, int count) {
        return getCoinsMessage(price, count, false);
    }

    public static Text getCoinsMessage(double price, int count, boolean preCounted) {
        // Format the price string once
        String priceString = String.format(Locale.ENGLISH, "%1$,.1f", preCounted ? price / count : price);

        // If count is 1, return a simple message
        if (count == 1) {
            return Text.literal(priceString + " Coins").formatted(Formatting.DARK_AQUA);
        }

        // If count is greater than 1, include the "each" information
        String priceStringTotal = String.format(Locale.ENGLISH, "%1$,.1f", preCounted ? price : price * count);

        return Text.literal(priceStringTotal + " Coins ").formatted(Formatting.DARK_AQUA)
                .append(Text.literal("(" + priceString + " each)").formatted(Formatting.GRAY));
    }

    // If these options is true beforehand, the client will get first data of these options while loading.
    // After then, it will only fetch the data if it is on Skyblock.
    public static int minute = 0;

    @Init
    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute) {
                sentNullWarning = false;
                return;
            }

            if (++minute % 60 == 0) {
                sentNullWarning = false;
            }

            CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE, TooltipInfoType.MOTES, TooltipInfoType.MUSEUM, TooltipInfoType.COLOR, TooltipInfoType.ACCESSORIES)
                    .map(DataTooltipInfoType.class::cast)
                    .map(DataTooltipInfoType::downloadIfEnabled)
                    .toArray(CompletableFuture[]::new)
            ).exceptionally(e -> {
                LOGGER.error("[Skyblocker] Encountered unknown error while downloading tooltip data", e);
                return null;
            });

            CraftPriceTooltip.clearCache();
        }, 1200, true);
    }
}
