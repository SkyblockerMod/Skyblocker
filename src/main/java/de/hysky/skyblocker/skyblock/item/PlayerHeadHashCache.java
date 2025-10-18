package de.hysky.skyblocker.skyblock.item;

import java.net.URI;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class PlayerHeadHashCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	/**
	 * Not all head texture hashes we need to cache are in the API so we need to manually add some for the skin transparency feature.
	 */
	private static final Set<String> MANUAL_CACHES = Set.of(
			HeadTextures.ADAPTIVE_BELT_HEALER,
			HeadTextures.ADAPTIVE_BELT_MAGE,
			HeadTextures.ADAPTIVE_BELT_BERSERK,
			HeadTextures.ADAPTIVE_BELT_ARCHER,
			HeadTextures.ADAPTIVE_BELT_TANK);
	private static final IntOpenHashSet CACHE = new IntOpenHashSet();

	protected static void loadSkins(JsonArray items) {
		try {
			Stream<String> apiItemTextures = items.asList().stream()
					.map(JsonElement::getAsJsonObject)
					.filter(item -> item.get("material").getAsString().equals("SKULL_ITEM"))
					.filter(item -> item.has("skin"))
					.map(item -> item.getAsJsonObject("skin").get("value").getAsString());
			Stream<String> overrideTextures = MANUAL_CACHES.stream();

			Stream.concat(apiItemTextures, overrideTextures)
			.map(PlayerHeadHashCache::getSkinHashFromBase64)
			.filter(hash -> hash != null && !hash.isEmpty())
			.mapToInt(String::hashCode)
			.forEach(CACHE::add);

			LOGGER.info("[Skyblocker Player Head Hash Cache] Successfully cached the hashes of all player head items!");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Failed to cache skin hashes!", e);
		}
	}

	public static String getSkinHashFromBase64(String base64) {
		try {
			String decoded = new String(Base64.getDecoder().decode(base64));
			JsonObject profile = JsonParser.parseString(decoded).getAsJsonObject();
			String url = profile.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

			return getSkinHashFromUrl(url);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Player Head Hash Cache] Error parsing head's base64 '{}'.", base64, e);
		}

		return "";
	}

	//From MinecraftProfileTexture#getHash
	public static String getSkinHashFromUrl(String url) {
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
