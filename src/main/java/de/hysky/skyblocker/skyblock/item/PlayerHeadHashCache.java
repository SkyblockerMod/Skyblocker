package de.hysky.skyblocker.skyblock.item;

import java.net.URI;
import java.util.Base64;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class PlayerHeadHashCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final IntOpenHashSet CACHE = new IntOpenHashSet();

	static void loadSkins(JsonArray items) {
		try {
			items.asList().stream()
			.map(JsonElement::getAsJsonObject)
			.filter(item -> item.get("material").getAsString().equals("SKULL_ITEM"))
			.filter(item -> item.has("skin"))
			.map(item -> Base64.getDecoder().decode(item.getAsJsonObject("skin").get("value").getAsString()))
			.map(String::new)
			.map(profile -> JsonParser.parseString(profile).getAsJsonObject())
			.map(profile -> profile.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString())
			.map(PlayerHeadHashCache::getSkinHash)
			.filter(hash -> hash != null && !hash.isEmpty())
			.mapToInt(String::hashCode)
			.forEach(CACHE::add);

			LOGGER.info("[Skyblocker Player Head Hash Cache] Successfully cached the hashes of all player head items!");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Failed to cache skin hashes!", e);
		}
	}

	//From MinecraftProfileTexture#getHash
	public static String getSkinHash(String url) {
		if (url != null && url.equals("ETF pre test, skin check")) {
			return "";
		}

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
