package de.hysky.skyblocker.skyblock.item.tooltip;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public enum TooltipInfoType implements Runnable {
    NPC("https://hysky.de/api/npcprice", itemTooltip -> itemTooltip.enableNPCPrice, true),
    BAZAAR("https://hysky.de/api/bazaar", itemTooltip -> itemTooltip.enableBazaarPrice || SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().general.chestValue.enableChestValue, itemTooltip -> itemTooltip.enableBazaarPrice, false),
    LOWEST_BINS("https://hysky.de/api/auctions/lowestbins", itemTooltip -> itemTooltip.enableLowestBIN || SkyblockerConfigManager.get().locations.dungeons.dungeonChestProfit.enableProfitCalculator || SkyblockerConfigManager.get().general.chestValue.enableChestValue, itemTooltip -> itemTooltip.enableLowestBIN, false),
    ONE_DAY_AVERAGE("https://moulberry.codes/auction_averages_lbin/1day.json", itemTooltip -> itemTooltip.enableAvgBIN, false),
    THREE_DAY_AVERAGE("https://moulberry.codes/auction_averages_lbin/3day.json", itemTooltip -> itemTooltip.enableAvgBIN, false),
    MOTES("https://hysky.de/api/motesprice", itemTooltip -> itemTooltip.enableMotesPrice, itemTooltip -> itemTooltip.enableMotesPrice && Utils.isInTheRift(), true),
    OBTAINED(itemTooltip -> itemTooltip.enableObtainedDate),
    MUSEUM("https://hysky.de/api/museum", itemTooltip -> itemTooltip.enableMuseumInfo, true),
    COLOR("https://hysky.de/api/color", itemTooltip -> itemTooltip.enableExoticTooltip, true);

    private final String address;
    private final Predicate<SkyblockerConfig.ItemTooltip> dataEnabled;
    private final Predicate<SkyblockerConfig.ItemTooltip> tooltipEnabled;
    private JsonObject data;
    private final boolean cacheable;
    private long hash;

    /**
     * Use this for when you're adding tooltip info that has no data associated with it
     */
    TooltipInfoType(Predicate<SkyblockerConfig.ItemTooltip> enabled) {
        this(null, itemTooltip -> false, enabled, null, false);
    }

    /**
     * @param address   the address to download the data from
     * @param enabled   the predicate to check if the data should be downloaded and the tooltip should be shown
     * @param cacheable whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<SkyblockerConfig.ItemTooltip> enabled, boolean cacheable) {
        this(address, enabled, enabled, null, cacheable);
    }

    /**
     * @param address        the address to download the data from
     * @param dataEnabled    the predicate to check if data should be downloaded
     * @param tooltipEnabled the predicate to check if the tooltip should be shown
     * @param cacheable      whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<SkyblockerConfig.ItemTooltip> dataEnabled, Predicate<SkyblockerConfig.ItemTooltip> tooltipEnabled, boolean cacheable) {
        this(address, dataEnabled, tooltipEnabled, null, cacheable);
    }

    /**
     * @param address        the address to download the data from
     * @param dataEnabled    the predicate to check if data should be downloaded
     * @param tooltipEnabled the predicate to check if the tooltip should be shown
     * @param data           the data
     * @param cacheable      whether the data should be cached
     */
    TooltipInfoType(String address, Predicate<SkyblockerConfig.ItemTooltip> dataEnabled, Predicate<SkyblockerConfig.ItemTooltip> tooltipEnabled, @Nullable JsonObject data, boolean cacheable) {
        this.address = address;
        this.dataEnabled = dataEnabled;
        this.tooltipEnabled = tooltipEnabled;
        this.data = data;
        this.cacheable = cacheable;
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
            data = SkyblockerMod.GSON.fromJson(Http.sendGetRequest(address), JsonObject.class);
        } catch (Exception e) {
            ItemTooltip.LOGGER.warn("[Skyblocker] Failed to download " + this + " prices!", e);
        }
    }
}
