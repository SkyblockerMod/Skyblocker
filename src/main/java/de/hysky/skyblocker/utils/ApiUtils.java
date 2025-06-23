package de.hysky.skyblocker.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonParser;
import com.mojang.util.UndashedUuid;

import de.hysky.skyblocker.utils.Http.ApiResponse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

/*
 * Contains only basic helpers for using Http APIs
 */
public class ApiUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiUtils.class);
	/**
	 * Similar to how the Auth Lib caches GameProfiles.
	 */
	private static final LoadingCache<String, String> NAME_2_UUID_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(20, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public String load(String key) throws Exception {
					return name2UuidInternal(key, 0);
				}
			});

	/**
	 * Multithreading is to be handled by the method caller
	 */
	public static String name2Uuid(String name) {
		return NAME_2_UUID_CACHE.getUnchecked(name);
	}

	private static String name2UuidInternal(String name, int retries) {
		Session session = MinecraftClient.getInstance().getSession();

		if (session.getUsername().equalsIgnoreCase(name)) {
			return UndashedUuid.toString(session.getUuidOrNull());
		}

		try (ApiResponse response = Http.sendName2UuidRequest(name)) {
			if (response.ok()) {
				return JsonParser.parseString(response.content()).getAsJsonObject().get("id").getAsString();
			} else if (response.ratelimited() && retries < 3) {
				Thread.sleep(800);

				return name2UuidInternal(name, ++retries);
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Name to uuid lookup failed! Name: {}", name, e);
		}

		return "";
	}
}
