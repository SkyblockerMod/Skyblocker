package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer2.ProfileViewer;
import de.hysky.skyblocker.utils.Http;

public class EliteLeaderboards {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final LoadingCache<CacheKey, Map<String, Integer>> LEADERBOARDS_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(5L))
			.build(new CacheLoader<>() {
				@Override
				public Map<String, Integer> load(CacheKey key) throws Exception {
					return fetchPlayerLeaderboardsInternal(key);
				}
			});
	public static final int NO_POSITION = -1;

	/// Mapping of collection ids to Elite leaderboard ids.
	///
	/// Example: INK_SAC:3 -> lapis
	private static Map<String, String> collectionLeaderboards = Map.of();

	@Init
	public static void init() {
		if (!ProfileViewer.ENABLED) return;

		CompletableFuture.supplyAsync(EliteLeaderboards::fetchLeaderboardsList).thenAccept(EliteLeaderboards::collectCollectionsLeaderboards);
	}

	/// {@return a mapping of collection ids to Elite leaderboard ids}
	public static Map<String, String> getCollectionLeaderboardMappings() {
		return collectionLeaderboards;
	}

	private static @Nullable Map<String, LeaderboardInfo> fetchLeaderboardsList() {
		try {
			String response = Http.sendGetRequest("https://api.eliteskyblock.com/leaderboards");
			JsonObject leaderboardsObject = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("leaderboards");
			Map<String, LeaderboardInfo> leaderboards = LeaderboardInfo.MAP_CODEC.parse(JsonOps.INSTANCE, leaderboardsObject).getOrThrow();

			return leaderboards;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to load Elite leaderboards.", e);

			return null;
		}
	}

	private static void collectCollectionsLeaderboards(@Nullable Map<String, LeaderboardInfo> leaderboards) {
		// The data failed to load sadly
		if (leaderboards == null) {
			return;
		}

		Map<String, String> collectionLeaderboardMapping = new HashMap<>();

		for (Map.Entry<String, LeaderboardInfo> entry : leaderboards.entrySet()) {
			String id = entry.getKey();
			LeaderboardInfo info = entry.getValue();

			if (info.interval().toLowerCase(Locale.ENGLISH).equals("current") && info.itemId().isPresent()) {
				collectionLeaderboardMapping.put(info.itemId().get(), id);
			}
		}

		collectionLeaderboards = Map.copyOf(collectionLeaderboardMapping);
	}

	public static CompletableFuture<Map<String, Integer>> fetchPlayerLeaderboards(String uuid, UUID profileId) {
		return CompletableFuture.supplyAsync(() -> LEADERBOARDS_CACHE.getUnchecked(new CacheKey(uuid, profileId)));
	}

	private static Map<String, Integer> fetchPlayerLeaderboardsInternal(CacheKey key) {
		try {
			String url = String.format("https://api.eliteskyblock.com/leaderboards/%s/%s", key.uuid(), key.profileId());
			String response = Http.sendGetRequest(url);
			JsonObject data = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("ranks");

			Map<String, Integer> positions = new HashMap<>();

			for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
				String id = entry.getKey();

				// When you are unranked the object is null
				if (!entry.getValue().isJsonNull()) {
					int position = entry.getValue().getAsJsonObject().get("rank").getAsInt();

					positions.put(id, position);
				}
			}

			return Map.copyOf(positions);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to load Elite leaderboard data for player {} with profile {}.", key.uuid(), key.profileId(), e);

			return Map.of();
		}
	}

	private record LeaderboardInfo(Optional<String> itemId, String interval) {
		private static final Codec<LeaderboardInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("itemId").forGetter(LeaderboardInfo::itemId),
				Codec.STRING.fieldOf("interval").forGetter(LeaderboardInfo::interval)
				).apply(instance, LeaderboardInfo::new));
		private static final Codec<Map<String, LeaderboardInfo>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
	}

	private record CacheKey(String uuid, UUID profileId) {}
}
