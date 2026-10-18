package de.hysky.skyblocker.utils.networth;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.slf4j.Logger;

import net.azureaaron.networth.data.SkyblockItemData;

import de.hysky.skyblocker.utils.ItemUtils;

public class NetworthDataSuppliers {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static Object2ObjectMap<String, SkyblockItemData> itemData = Object2ObjectMaps.emptyMap();

	public static void updateSkyblockItemData(JsonArray items) {
		try {
			itemData = SkyblockItemData.MAP_CODEC.parse(JsonOps.INSTANCE, items).getOrThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Networth Data Supplier] Failed to parse items data!", e);
		}
	}

	static Object2ObjectMap<String, SkyblockItemData> getSkyblockItemData() {
		return itemData;
	}

	static double getPrice(String id) {
		// Use bazaar buy price because sell price can be heavily skewed sometimes
		// Use three day average prices to avoid auction manipulation
		return ItemUtils.getItemPrice(id, true, true).orElse(0);
	}
}
