package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public enum TooltipInfoType implements Runnable {
    NPC("https://hysky.de/api/npcprice", itemTooltip -> itemTooltip.enableNPCPrice, true),
    BAZAAR("https://hysky.de/api/bazaar", itemTooltip -> itemTooltip.enableBazaarPrice || itemTooltip.enableCraftingCost.getOrder() != null || itemTooltip.enableEstimatedItemValue || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit || SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue, itemTooltip -> itemTooltip.enableBazaarPrice, false),
    LOWEST_BINS("https://hysky.de/api/auctions/lowestbins", itemTooltip -> itemTooltip.enableLowestBIN || itemTooltip.enableCraftingCost.getOrder() != null || itemTooltip.enableEstimatedItemValue || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().dungeons.dungeonChestProfit.croesusProfit || SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue, itemTooltip -> itemTooltip.enableLowestBIN, false),
    ONE_DAY_AVERAGE("https://hysky.de/api/auctions/lowestbins/average/1day.json", itemTooltip -> itemTooltip.enableAvgBIN, false),
    THREE_DAY_AVERAGE("https://hysky.de/api/auctions/lowestbins/average/3day.json", itemTooltip -> itemTooltip.enableAvgBIN || SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableAuctionHouse, itemTooltip -> itemTooltip.enableAvgBIN, false),
    MOTES("https://hysky.de/api/motesprice", itemTooltip -> itemTooltip.enableMotesPrice, itemTooltip -> itemTooltip.enableMotesPrice && Utils.isInTheRift(), true),
    OBTAINED(itemTooltip -> itemTooltip.enableObtainedDate),
    MUSEUM("https://hysky.de/api/museum", itemTooltip -> itemTooltip.enableMuseumInfo, true),
    COLOR("https://hysky.de/api/color", itemTooltip -> itemTooltip.enableExoticTooltip, true),
    ACCESSORIES("https://hysky.de/api/accessories", itemTooltip -> itemTooltip.enableAccessoriesHelper, true, AccessoriesHelper::refreshData),
    ESTIMATED_ITEM_VALUE(itemTooltip -> itemTooltip.enableEstimatedItemValue);

    private final String address;
    private final Predicate<GeneralConfig.ItemTooltip> dataEnabled;
    private final Predicate<GeneralConfig.ItemTooltip> tooltipEnabled;
    private JsonObject data;
    private final boolean cacheable;
    private long hash;
    private final Consumer<JsonObject> callback;

    /**
     * Use this for when you're adding tooltip info that has no data associated with it
     */
    TooltipInfoType(Predicate<GeneralConfig.ItemTooltip> enabled) {
        this(null, itemTooltip -> false, enabled, false, null);
    }

    /**
     * @param address   the address to download the data from
     * @param enabled   the predicate to check if the data should be downloaded and the tooltip should be shown
     * @param cacheable whether the data should be cached
     * @param callback  called when the {@code data} is refreshed
     */
    TooltipInfoType(String address, Predicate<GeneralConfig.ItemTooltip> enabled, boolean cacheable, Consumer<JsonObject> callback) {
        this(address, enabled, enabled, cacheable, callback);
    }

    /**
     * @param address   the address to download the data from
     * @param enabled   the predicate to check if the data should be downloaded and the tooltip should be shown
     * @param cacheable whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<GeneralConfig.ItemTooltip> enabled, boolean cacheable) {
        this(address, enabled, enabled, cacheable, null);
    }

    /**
     * @param address        the address to download the data from
     * @param dataEnabled    the predicate to check if data should be downloaded
     * @param tooltipEnabled the predicate to check if the tooltip should be shown
     * @param cacheable      whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<GeneralConfig.ItemTooltip> dataEnabled, Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, boolean cacheable) {
        this(address, dataEnabled, tooltipEnabled, cacheable, null);
    }

    /**
     * @param address        the address to download the data from
     * @param dataEnabled    the predicate to check if data should be downloaded
     * @param tooltipEnabled the predicate to check if the tooltip should be shown
     * @param cacheable      whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<GeneralConfig.ItemTooltip> dataEnabled, Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, boolean cacheable, @Nullable Consumer<JsonObject> callback) {
        this.address = address;
        this.dataEnabled = dataEnabled;
        this.tooltipEnabled = tooltipEnabled;
        this.data = null;
        this.cacheable = cacheable;
        this.callback = callback;
    }

    /**
     * @return whether the data should be downloaded
     */
    private boolean isDataEnabled() {
        return dataEnabled.test(ItemTooltip.config);
    }

    /**
     * @return whether the tooltip should be shown
     */
    public boolean isTooltipEnabled() {
        return tooltipEnabled.test(ItemTooltip.config);
    }

    public JsonObject getData() {
        return data;
    }

    /**
     * Checks if the data has the given member name and sends a warning message if data is null.
     *
     * @param memberName the member name to check
     * @return whether the data has the given member name or not
     */
    public boolean hasOrNullWarning(String memberName) {
        if (data == null) {
            ItemTooltip.nullWarning();
            return false;
        } else return data.has(memberName);
    }

    /**
     * Checks if the tooltip is enabled and the data has the given member name and sends a warning message if data is null.
     *
     * @param memberName the member name to check
     * @return whether the tooltip is enabled and the data has the given member name or not
     */
    public boolean isTooltipEnabledAndHasOrNullWarning(String memberName) {
        return isTooltipEnabled() && hasOrNullWarning(memberName);
    }

    /**
     * Downloads the data if it is enabled.
     *
     * @param futureList the list to add the future to
     */
    public void downloadIfEnabled(List<CompletableFuture<Void>> futureList) {
        if (isDataEnabled()) {
            download(futureList);
        }
    }

    /**
     * Downloads the data.
     *
     * @param futureList the list to add the future to
     */
    public void download(List<CompletableFuture<Void>> futureList) {
        futureList.add(CompletableFuture.runAsync(this));
    }

    /**
     * Downloads the data.
     */
    @Override
    public void run() {
        try {
            if (cacheable) {
                HttpHeaders headers = Http.sendHeadRequest(address);
                long hash = Http.getEtag(headers).hashCode() + Http.getLastModified(headers).hashCode();
                if (this.hash == hash) return;
                else this.hash = hash;
            }
            String response = Http.sendGetRequest(address);
            if (response.trim().startsWith("<!DOCTYPE") || response.trim().startsWith("<html")) {
                ItemTooltip.LOGGER.warn("[Skyblocker] Received HTML content for " + this.name() + ". Expected JSON.");
                return;
            }
            data = SkyblockerMod.GSON.fromJson(response, JsonObject.class);

            if (callback != null) callback.accept(data);
        } catch (Exception e) {
            ItemTooltip.LOGGER.warn("[Skyblocker] Failed to download " + this + " prices!", e);
        }
    }
}
