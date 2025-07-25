package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProfileUtils {
	public static final Logger LOGGER = LoggerFactory.getLogger(ProfileUtils.class);
	private static final long HYPIXEL_API_COOLDOWN = 300000; // 5min = 300000

	public static Map<String, ObjectLongPair<JsonObject>> players = new HashMap<>();
	public static Map<String, ObjectLongPair<JsonObject>> profiles = new HashMap<>();

	public static CompletableFuture<JsonObject> updateProfileByName(String name) {
		return fetchFullProfile(name).thenApply(profile -> {
			JsonObject player = profile.getAsJsonArray("profiles").asList().stream()
					.map(JsonElement::getAsJsonObject)
					.filter(profileObj -> profileObj.getAsJsonPrimitive("selected").getAsBoolean())
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("No selected profile found!?"))
					.getAsJsonObject("members").get(name).getAsJsonObject();

			players.put(name, ObjectLongPair.of(player, System.currentTimeMillis()));
			return player;
		});
	}

	public static CompletableFuture<JsonObject> fetchFullProfile(String name) {
		ObjectLongPair<JsonObject> playerCache = profiles.get(name);
		if (playerCache != null && playerCache.rightLong() + HYPIXEL_API_COOLDOWN > System.currentTimeMillis()) {
			return CompletableFuture.completedFuture(playerCache.left());
		}

		return CompletableFuture.supplyAsync(() -> {
			String uuid = ApiUtils.name2Uuid(name);
			try (Http.ApiResponse response = Http.sendHypixelRequest("skyblock/profiles", "?uuid=" + uuid)) {
				if (!response.ok()) {
					throw new IllegalStateException("Failed to get profile uuid for player: " + name + "! Response: " + response.content());
				}
				JsonObject profile = SkyblockerMod.GSON.fromJson(response.content(), JsonObject.class);
				profiles.put(name, ObjectLongPair.of(profile, System.currentTimeMillis()));

				return profile;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profile Utils] Failed to get Player Profile Data for players {}, is the API Down/Limited?", name, e);
			}
			return null;
		});
	}
}
