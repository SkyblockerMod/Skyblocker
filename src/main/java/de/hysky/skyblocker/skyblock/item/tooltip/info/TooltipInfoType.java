package de.hysky.skyblocker.skyblock.item.tooltip.info;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.config.configs.GeneralConfig.Craft;
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper.Accessory;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.EssenceShopPrice;
import de.hysky.skyblocker.utils.BazaarProduct;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public interface TooltipInfoType {
	DataTooltipInfoType<Object2DoubleMap<String>> NPC = ofData("https://hysky.de/api/npcprice", CodecUtils.object2DoubleMapCodec(Codec.STRING), true, Object2DoubleMap::containsKey, itemTooltip -> itemTooltip.enableNPCPrice);
	DataTooltipInfoType<Object2ObjectMap<String, BazaarProduct>> BAZAAR = ofData("https://hysky.de/api/bazaar", BazaarProduct.MAP_CODEC, false, Object2ObjectMap::containsKey, itemTooltip -> itemTooltip.enableBazaarPrice, itemTooltip -> itemTooltip.enableBazaarPrice || itemTooltip.enableCraftingCost != Craft.OFF || itemTooltip.enableEstimatedItemValue || getConfig().dungeons.dungeonChestProfit.enableProfitCalculator || getConfig().dungeons.dungeonChestProfit.croesusProfit || getConfig().uiAndVisuals.chestValue.enableChestValue || itemTooltip.showEssenceCost, EssenceShopPrice::refreshEssencePrices);
	DataTooltipInfoType<Object2DoubleMap<String>> LOWEST_BINS = ofData("https://hysky.de/api/auctions/lowestbins", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey, itemTooltip -> itemTooltip.enableLowestBIN, itemTooltip -> itemTooltip.enableLowestBIN || itemTooltip.enableCraftingCost != Craft.OFF || itemTooltip.enableEstimatedItemValue || getConfig().dungeons.dungeonChestProfit.enableProfitCalculator || getConfig().dungeons.dungeonChestProfit.croesusProfit || getConfig().uiAndVisuals.chestValue.enableChestValue);
	DataTooltipInfoType<Object2DoubleMap<String>> ONE_DAY_AVERAGE = ofData("https://hysky.de/api/auctions/lowestbins/average/1day.json", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey, itemTooltip -> itemTooltip.enableAvgBIN, itemTooltip -> itemTooltip.enableAvgBIN && itemTooltip.avg != GeneralConfig.Average.THREE_DAY);
	DataTooltipInfoType<Object2DoubleMap<String>> THREE_DAY_AVERAGE = ofData("https://hysky.de/api/auctions/lowestbins/average/3day.json", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey, itemTooltip -> itemTooltip.enableAvgBIN, itemTooltip -> itemTooltip.enableAvgBIN && itemTooltip.avg != GeneralConfig.Average.ONE_DAY || getConfig().uiAndVisuals.searchOverlay.enableAuctionHouse);
	DataTooltipInfoType<Object2IntMap<String>> MOTES = ofData("https://hysky.de/api/motesprice", CodecUtils.object2IntMapCodec(Codec.STRING), true, Object2IntMap::containsKey, itemTooltip -> itemTooltip.enableMotesPrice, itemTooltip -> itemTooltip.enableMotesPrice && Utils.isInTheRift());
	TooltipInfoType OBTAINED = ofSimple(itemTooltip -> itemTooltip.enableObtainedDate);
	DataTooltipInfoType<Map<String, String>> MUSEUM = ofData("https://hysky.de/api/museum", Codec.unboundedMap(Codec.STRING, Codec.STRING), true, Map::containsKey, itemTooltip -> itemTooltip.enableMuseumInfo);
	DataTooltipInfoType<Map<String, String>> COLOR = ofData("https://hysky.de/api/color", Codec.unboundedMap(Codec.STRING, Codec.STRING), true, Map::containsKey, itemTooltip -> itemTooltip.enableExoticTooltip);
	DataTooltipInfoType<Map<String, Accessory>> ACCESSORIES = ofData("https://hysky.de/api/accessories", Accessory.MAP_CODEC, true, Map::containsKey, itemTooltip -> itemTooltip.enableAccessoriesHelper, AccessoriesHelper::refreshData);
	TooltipInfoType ESTIMATED_ITEM_VALUE = ofSimple(itemTooltip -> itemTooltip.enableEstimatedItemValue);

	/**
	 * @return whether the tooltip should be shown
	 */
	boolean isTooltipEnabled();

	private static SkyblockerConfig getConfig() {
		return SkyblockerConfigManager.get();
	}

	private static TooltipInfoType ofSimple(Predicate<GeneralConfig.ItemTooltip> tooltipEnabled) {
		return new SimpleTooltipInfo(tooltipEnabled);
	}

	@SafeVarargs
	private static <T> DataTooltipInfoType<T> ofData(String address, Codec<T> codec, boolean cacheable, BiPredicate<T, String> contains, Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, Consumer<T>... callbacks) {
		return ofData(address, codec, cacheable, contains, tooltipEnabled, tooltipEnabled, callbacks);
	}

	@SafeVarargs
	private static <T> DataTooltipInfoType<T> ofData(String address, Codec<T> codec, boolean cacheable, BiPredicate<T, String> contains, Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, Predicate<GeneralConfig.ItemTooltip> dataEnabled, Consumer<T>... callbacks) {
		return new DataTooltipInfo<>(address, codec, cacheable, contains, tooltipEnabled, dataEnabled, callbacks);
	}
}
