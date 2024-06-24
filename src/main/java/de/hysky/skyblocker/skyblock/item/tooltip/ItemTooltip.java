package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ItemTooltip {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ItemTooltip.class.getName());
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static final GeneralConfig.ItemTooltip config = SkyblockerConfigManager.get().general.itemTooltip;
    private static volatile boolean sentNullWarning = false;

    /**
     * Gets the NEU id from an id and an api id.
     * @param id the id of the skyblock item, gotten from {@link de.hysky.skyblocker.utils.ItemUtils#getItemId(net.minecraft.item.ItemStack) ItemUtils#getItemId(ItemStack)} or {@link net.minecraft.item.ItemStack#getSkyblockId() ItemStack#getSkyblockId()}
     * @param apiId the api id of the skyblock item, matching the id of the item on the Skyblocker api, gotten from {@link net.minecraft.item.ItemStack#getSkyblockApiId() ItemStack#getSkyblockApiId()}
     * @return the NEU id of the skyblock item, matching the id of the item gotten from {@link io.github.moulberry.repo.data.NEUItem#getSkyblockItemId() NEUItem#getSkyblockItemId()} or {@link net.minecraft.item.ItemStack#getNeuName() ItemStack#getNeuName()},
     * or an empty string if either id or apiId is null
     */
    @NotNull
    public static String getNeuName(String id, String apiId) {
        if (id == null || apiId == null) return "";
        switch (id) {
            case "PET" -> {
                apiId = apiId.replaceAll("LVL_\\d*_", "");
                String[] parts = apiId.split("_");
                String type = parts[0];
                apiId = apiId.replaceAll(type + "_", "");
                apiId = apiId + "-" + type;
                apiId = apiId.replace("UNCOMMON", "1")
                        .replace("COMMON", "0")
                        .replace("RARE", "2")
                        .replace("EPIC", "3")
                        .replace("LEGENDARY", "4")
                        .replace("MYTHIC", "5")
                        .replace("-", ";");
            }
            case "RUNE" -> apiId = apiId.replaceAll("_(?!.*_)", ";");
            case "POTION" -> apiId = "";
            case "ATTRIBUTE_SHARD" ->
                    apiId = id + "+" + apiId.replace("SHARD-", "").replaceAll("_(?!.*_)", ";");
            case "NEW_YEAR_CAKE" -> apiId = id + "+" + apiId.replace("NEW_YEAR_CAKE_", "");
            case "PARTY_HAT_CRAB_ANIMATED" -> apiId = "PARTY_HAT_CRAB_" + apiId.replace("PARTY_HAT_CRAB_ANIMATED_", "") + "_ANIMATED";
            case "CRIMSON_HELMET", "CRIMSON_CHESTPLATE", "CRIMSON_LEGGINGS", "CRIMSON_BOOTS",
            "AURORA_HELMET", "AURORA_CHESTPLATE", "AURORA_LEGGINGS", "AURORA_BOOTS",
            "TERROR_HELMET", "TERROR_CHESTPLATE", "TERROR_LEGGINGS", "TERROR_BOOTS" -> apiId = id;
            case "MIDAS_SWORD", "MIDAS_STAFF" -> apiId = id;
            default -> apiId = apiId.replace(":", "-");
        }
        return apiId;
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

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(() -> {
            if (!Utils.isOnSkyblock() && 0 < minute) {
                sentNullWarning = false;
                return;
            }

            if (++minute % 60 == 0) {
                sentNullWarning = false;
            }

            List<CompletableFuture<Void>> futureList = new ArrayList<>();

            TooltipInfoType.NPC.downloadIfEnabled(futureList);
            TooltipInfoType.BAZAAR.downloadIfEnabled(futureList);
            TooltipInfoType.LOWEST_BINS.downloadIfEnabled(futureList);

            if (config.enableAvgBIN) {
                GeneralConfig.Average type = config.avg;

                if (type == GeneralConfig.Average.BOTH || TooltipInfoType.ONE_DAY_AVERAGE.getData() == null || TooltipInfoType.THREE_DAY_AVERAGE.getData() == null || minute % 5 == 0) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                } else if (type == GeneralConfig.Average.ONE_DAY) {
                    TooltipInfoType.ONE_DAY_AVERAGE.download(futureList);
                } else if (type == GeneralConfig.Average.THREE_DAY) {
                    TooltipInfoType.THREE_DAY_AVERAGE.download(futureList);
                }
            }

            TooltipInfoType.MOTES.downloadIfEnabled(futureList);
            TooltipInfoType.MUSEUM.downloadIfEnabled(futureList);
            TooltipInfoType.COLOR.downloadIfEnabled(futureList);
            TooltipInfoType.ACCESSORIES.downloadIfEnabled(futureList);

            CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new)).exceptionally(e -> {
                LOGGER.error("Encountered unknown error while downloading tooltip data", e);
                return null;
            });

            CraftPriceTooltip.clearCache();
        }, 1200, true);
    }
}