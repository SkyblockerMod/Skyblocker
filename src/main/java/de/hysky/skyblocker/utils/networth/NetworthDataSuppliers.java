package de.hysky.skyblocker.utils.networth;

import java.util.List;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.utils.ItemUtils;
import net.azureaaron.networth.data.SkyblockItemData;

public class NetworthDataSuppliers {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static List<SkyblockItemData> itemData = List.of();

	public static void updateSkyblockItemData(JsonArray items) {
		try {
			itemData = SkyblockItemData.LIST_CODEC.parse(JsonOps.INSTANCE, items).getOrThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Networth Data Supplier] Failed to parse items data!", e);
		}
	}

	static List<SkyblockItemData> getSkyblockItemData() {
		return itemData;
	}

	static double getPrice(String id) {
		return ItemUtils.getItemPrice(id, true).leftDouble(); //Use bazaar buy price because sell price can be heavily skewed sometimes
	}
}
