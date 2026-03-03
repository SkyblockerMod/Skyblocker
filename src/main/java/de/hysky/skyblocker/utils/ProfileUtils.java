package de.hysky.skyblocker.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.SkyblockerMod;

public class ProfileUtils {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final LoadingCache<String, JsonObject> UUID_TO_PROFILES_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public JsonObject load(String uuid) throws Exception {
					return fetchProfilesInternal(uuid);
				}
			});

	/**
	 * Fetches the given player's profiles and returns the player's data from their currently selected profile.
	 */
	public static CompletableFuture<@Nullable JsonObject> fetchProfileMember(String name) {
		return CompletableFuture.supplyAsync(() -> {
			String uuid = ApiUtils.name2Uuid(name);

			if (!uuid.isEmpty()) {
				JsonObject profile = UUID_TO_PROFILES_CACHE.getUnchecked(uuid);
				JsonObject player = profile.getAsJsonArray("profiles").asList().stream()
						.map(JsonElement::getAsJsonObject)
						.filter(profileObj -> profileObj.getAsJsonPrimitive("selected").getAsBoolean())
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("No selected profile found!?"))
						.getAsJsonObject("members").get(uuid).getAsJsonObject();

				return player;
			}

			return null;
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	/**
	 * Fetches the all of the given player's skyblock profiles from the API and returns the JSON response.
	 */
	public static CompletableFuture<@Nullable JsonObject> fetchFullProfile(String name) {
		return CompletableFuture.supplyAsync(() -> {
			String uuid = ApiUtils.name2Uuid(name);

			return !uuid.isEmpty() ? UUID_TO_PROFILES_CACHE.getUnchecked(uuid) : null;
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	/**
	 * Fetches the all of the given player's skyblock profiles from the API and returns the JSON response.
	 */
	public static CompletableFuture<@Nullable JsonObject> fetchFullProfileByUuid(String uuid) {
		return CompletableFuture.supplyAsync(() -> {
			return !uuid.isEmpty() ? UUID_TO_PROFILES_CACHE.getUnchecked(uuid) : null;
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static @Nullable JsonObject fetchProfilesInternal(String uuid) {
		try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + uuid)) {
			if (!response.ok()) {
				throw new IllegalStateException(String.format("Failed to get profile for player: %s!, Status Code: %d, Response: %s", uuid, response.statusCode(), response.content()));
			}

			return SkyblockerMod.GSON.fromJson(response.content(), JsonObject.class);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Utils] Failed to get Player Profile Data for player {}, is the API Down/Limited?", uuid, e);
		}

		return null;
	}
}
