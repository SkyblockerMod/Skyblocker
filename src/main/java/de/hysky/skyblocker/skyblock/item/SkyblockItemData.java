package de.hysky.skyblocker.skyblock.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.networth.NetworthDataSuppliers;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class SkyblockItemData {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void init() {
		updateItems().thenAcceptAsync(items -> {
			PlayerHeadHashCache.loadSkins(items);
			NetworthDataSuppliers.updateSkyblockItemData(items);
		});
	}

	private static CompletableFuture<JsonArray> updateItems() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String response = Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/items");
				JsonObject itemsData = JsonParser.parseString(response).getAsJsonObject();

				return itemsData.getAsJsonArray("items");
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Item Data Loader] Failed to load items data from the Hypixel API!", e);
			}

			//Complete the future exceptionally so that the other things don't run
			throw new IllegalStateException();
		});
	}
}
