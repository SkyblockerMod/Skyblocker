package de.hysky.skyblocker.skyblock.item;

import java.net.URI;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.utils.Http;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class PlayerHeadHashCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IntOpenHashSet CACHE = new IntOpenHashSet();

	public static void init() {
		CompletableFuture.runAsync(PlayerHeadHashCache::loadSkins);
	}

	private static void loadSkins() {
		try {
			String response = Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/items");
			JsonArray items = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("items");

			items.asList().stream()
			.map(JsonElement::getAsJsonObject)
			.filter(item -> item.get("material").getAsString().equals("SKULL_ITEM"))
			.filter(item -> item.has("skin"))
			.map(item -> Base64.getDecoder().decode(item.get("skin").getAsString()))
			.map(String::new)
			.map(profile -> JsonParser.parseString(profile).getAsJsonObject())
			.map(profile -> profile.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString())
			.map(PlayerHeadHashCache::getSkinHash)
			.mapToInt(String::hashCode)
			.forEach(CACHE::add);

			LOGGER.info("[Skyblocker Player Head Hash Cache] Successfully cached the hashes of all player head items!");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Failed to cache skin hashes!", e);
		}
	}

	//From MinecraftProfileTexture#getHash
	public static String getSkinHash(String url) {
		try {
			return FilenameUtils.getBaseName(new URI(url).getPath());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Malformed Skin URL! URL: {}", url, e);
		}

		return "";
	}

	public static boolean contains(int hash) {
		return CACHE.contains(hash);
	}
}
