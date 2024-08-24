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
	DataTooltipInfoType<Object2DoubleMap<String>> NPC = ofData(itemTooltip -> itemTooltip.enableNPCPrice, "https://hysky.de/api/npcprice", CodecUtils.object2DoubleMapCodec(Codec.STRING), true, Object2DoubleMap::containsKey);
	DataTooltipInfoType<Object2ObjectMap<String, BazaarProduct>> BAZAAR = ofData(itemTooltip -> itemTooltip.enableBazaarPrice, itemTooltip -> itemTooltip.enableBazaarPrice || itemTooltip.enableCraftingCost != Craft.OFF || itemTooltip.enableEstimatedItemValue || getConfig().dungeons.dungeonChestProfit.enableProfitCalculator || getConfig().dungeons.dungeonChestProfit.croesusProfit || getConfig().uiAndVisuals.chestValue.enableChestValue || itemTooltip.showEssenceCost, "https://hysky.de/api/bazaar", BazaarProduct.MAP_CODEC, false, Object2ObjectMap::containsKey, EssenceShopPrice::refreshEssencePrices);
	DataTooltipInfoType<Object2DoubleMap<String>> LOWEST_BINS = ofData(itemTooltip -> itemTooltip.enableLowestBIN, itemTooltip -> itemTooltip.enableLowestBIN || itemTooltip.enableCraftingCost != Craft.OFF || itemTooltip.enableEstimatedItemValue || getConfig().dungeons.dungeonChestProfit.enableProfitCalculator || getConfig().dungeons.dungeonChestProfit.croesusProfit || getConfig().uiAndVisuals.chestValue.enableChestValue, "https://hysky.de/api/auctions/lowestbins", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey);
	DataTooltipInfoType<Object2DoubleMap<String>> ONE_DAY_AVERAGE = ofData(itemTooltip -> itemTooltip.enableAvgBIN, "https://hysky.de/api/auctions/lowestbins/average/1day.json", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey);
	DataTooltipInfoType<Object2DoubleMap<String>> THREE_DAY_AVERAGE = ofData(itemTooltip -> itemTooltip.enableAvgBIN, itemTooltip -> itemTooltip.enableAvgBIN || getConfig().uiAndVisuals.searchOverlay.enableAuctionHouse, "https://hysky.de/api/auctions/lowestbins/average/3day.json", CodecUtils.object2DoubleMapCodec(Codec.STRING), false, Object2DoubleMap::containsKey);
	DataTooltipInfoType<Object2IntMap<String>> MOTES = ofData(itemTooltip -> itemTooltip.enableMotesPrice, itemTooltip -> itemTooltip.enableMotesPrice && Utils.isInTheRift(), "https://hysky.de/api/motesprice", CodecUtils.object2IntMapCodec(Codec.STRING), true, Object2IntMap::containsKey);
	TooltipInfoType OBTAINED = ofSimple(itemTooltip -> itemTooltip.enableObtainedDate);
	DataTooltipInfoType<Map<String, String>> MUSEUM = ofData(itemTooltip -> itemTooltip.enableMuseumInfo, "https://hysky.de/api/museum", Codec.unboundedMap(Codec.STRING, Codec.STRING), true, Map::containsKey);
	DataTooltipInfoType<Map<String, String>> COLOR = ofData(itemTooltip -> itemTooltip.enableExoticTooltip, "https://hysky.de/api/color", Codec.unboundedMap(Codec.STRING, Codec.STRING), true, Map::containsKey);
	DataTooltipInfoType<Map<String, Accessory>> ACCESSORIES = ofData(itemTooltip -> itemTooltip.enableAccessoriesHelper, "https://hysky.de/api/accessories", Accessory.MAP_CODEC, true, Map::containsKey, AccessoriesHelper::refreshData);
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
	private static <T> DataTooltipInfoType<T> ofData(Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, String address, Codec<T> codec, boolean cacheable, BiPredicate<T, String> contains, Consumer<T>... callbacks) {
		return ofData(tooltipEnabled, tooltipEnabled, address, codec, cacheable, contains, callbacks);
	}

	@SafeVarargs
	private static <T> DataTooltipInfoType<T> ofData(Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, Predicate<GeneralConfig.ItemTooltip> dataEnabled, String address, Codec<T> codec, boolean cacheable, BiPredicate<T, String> contains, Consumer<T>... callbacks) {
		return new DataTooltipInfo<>(tooltipEnabled, dataEnabled, address, codec, cacheable, contains, callbacks);
	}
}
