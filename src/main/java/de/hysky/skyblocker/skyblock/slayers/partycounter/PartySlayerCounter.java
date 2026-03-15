package de.hysky.skyblocker.skyblock.slayers.partycounter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public final class PartySlayerCounter {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CACHE_PATH = SkyblockerMod.CONFIG_DIR.resolve("party_counter_cache.json");
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final long DEATH_CONFIRM_WINDOW_MS = 2000L;
	private static final long DEATH_EXPIRE_MS = 5000L;

	private static final Map<String, Integer> KILL_COUNTS = new ConcurrentHashMap<>();
	private static final Map<String, UUID> ACTIVE_BOSSES = new ConcurrentHashMap<>();
	private static final Map<String, Long> BOSS_DEATH_TIMES = new ConcurrentHashMap<>();

	private static boolean wasInParty = false;
	private static Set<UUID> lastPartyMembers = new HashSet<>();

	private PartySlayerCounter() {
	}

	public static void initialize() {
		loadCache();
	}

	public static void tick() {
		boolean inParty = PartyTracker.isInParty();
		Set<UUID> currentMembers = PartyTracker.getPartyMemberUuids();

		if (wasInParty && !inParty) {
			onPartyDisband();
		} else if (inParty && !currentMembers.equals(lastPartyMembers)) {
			if (!lastPartyMembers.isEmpty() && currentMembers.isEmpty()) {
				onPartyDisband();
			} else if (!lastPartyMembers.isEmpty()) {
				validateCacheAgainstParty();
			}
		}

		wasInParty = inParty;
		lastPartyMembers = new HashSet<>(currentMembers);

		if (CLIENT.level != null) {
			long now = System.currentTimeMillis();
			ACTIVE_BOSSES.entrySet().removeIf(entry -> {
				UUID bossUuid = entry.getValue();
				for (Entity e : CLIENT.level.entitiesForRendering()) {
					if (e.getUUID().equals(bossUuid)) return false;
				}
				String key = entry.getKey();
				BOSS_DEATH_TIMES.put(key, now);
				LOGGER.debug("[Skyblocker Party Counter] Boss disappeared for {}, recording death time", key);
				return true;
			});

			BOSS_DEATH_TIMES.entrySet().removeIf(entry -> now - entry.getValue() > DEATH_EXPIRE_MS);
		}
	}

	public static void onBossDetected(String spawnerName, UUID bossUuid) {
		if (spawnerName == null || spawnerName.isEmpty()) return;
		if (!PartyTracker.isInParty()) return;
		if (isLocalPlayer(spawnerName)) return;
		if (!isConfirmedPartyMember(spawnerName)) return;

		String key = spawnerName.toLowerCase(Locale.ROOT);
		ACTIVE_BOSSES.put(key, bossUuid);
		BOSS_DEATH_TIMES.remove(key);
		LOGGER.debug("[Skyblocker Party Counter] Tracking boss for: {} (UUID: {})", spawnerName, bossUuid);
	}

	public static boolean wasTrackingBoss(String spawnerName) {
		String key = spawnerName.toLowerCase(Locale.ROOT);
		if (ACTIVE_BOSSES.containsKey(key)) return true;
		@Nullable Long deathTime = BOSS_DEATH_TIMES.get(key);
		if (deathTime == null) return false;
		return System.currentTimeMillis() - deathTime <= DEATH_CONFIRM_WINDOW_MS;
	}

	public static void onBossKilled(String spawnerName) {
		if (spawnerName == null || spawnerName.isEmpty()) return;
		if (isLocalPlayer(spawnerName)) return;
		if (!isConfirmedPartyMember(spawnerName)) return;

		String key = spawnerName.toLowerCase(Locale.ROOT);
		KILL_COUNTS.merge(spawnerName, 1, Integer::sum);
		saveCache();
		LOGGER.info("[Skyblocker Party Counter] Boss killed for {}, total: {}", spawnerName, KILL_COUNTS.get(spawnerName));
		ACTIVE_BOSSES.remove(key);
		BOSS_DEATH_TIMES.remove(key);
	}

	public static void onBossFailed() {
		ACTIVE_BOSSES.clear();
		BOSS_DEATH_TIMES.clear();
	}

	public static void onPartyDisband() {
		KILL_COUNTS.clear();
		ACTIVE_BOSSES.clear();
		BOSS_DEATH_TIMES.clear();
		saveCache();
		LOGGER.info("[Skyblocker Party Counter] Party disbanded, counter cleared");
	}

	private static void validateCacheAgainstParty() {
		if (!PartyTracker.isInParty()) {
			KILL_COUNTS.clear();
			saveCache();
			return;
		}
		if (CLIENT.level == null) return;

		Set<String> validNames = new HashSet<>();
		for (var player : CLIENT.level.players()) {
			if (PartyTracker.isPartyMember(player.getUUID())) {
				validNames.add(player.getGameProfile().name().toLowerCase(Locale.ROOT));
			}
		}

		boolean changed = KILL_COUNTS.keySet().removeIf(name -> !validNames.contains(name.toLowerCase(Locale.ROOT)));
		if (changed) {
			saveCache();
		}
	}

	public static Map<String, Integer> getKillCounts() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(KILL_COUNTS));
	}

	public static int getTotalKills() {
		return KILL_COUNTS.values().stream().mapToInt(Integer::intValue).sum();
	}

	public static void clearCounter() {
		KILL_COUNTS.clear();
		ACTIVE_BOSSES.clear();
		BOSS_DEATH_TIMES.clear();
		saveCache();
	}

	public static int addKillCount(String playerName, int amount) {
		if (amount <= 0) return KILL_COUNTS.getOrDefault(playerName, 0);
		int newCount = KILL_COUNTS.merge(playerName, amount, Integer::sum);
		saveCache();
		return newCount;
	}

	public static int removeKillCount(String playerName, int amount) {
		@Nullable Integer current = KILL_COUNTS.get(playerName);
		if (current == null || current <= 0 || amount <= 0) return current != null ? current : 0;
		int newCount = current - amount;
		if (newCount <= 0) {
			KILL_COUNTS.remove(playerName);
			newCount = 0;
		} else {
			KILL_COUNTS.put(playerName, newCount);
		}
		saveCache();
		return newCount;
	}

	private static void loadCache() {
		try {
			if (Files.exists(CACHE_PATH)) {
				String json = Files.readString(CACHE_PATH);
				Type type = new TypeToken<Map<String, Integer>>() {}.getType();
				Map<String, Integer> loaded = GSON.fromJson(json, type);
				if (loaded != null) {
					KILL_COUNTS.clear();
					KILL_COUNTS.putAll(loaded);
					LOGGER.info("[Skyblocker Party Counter] Loaded cache with {} entries", KILL_COUNTS.size());
				}
			}
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Party Counter] Failed to load cache", e);
		}
	}

	private static void saveCache() {
		try {
			Path parentDir = CACHE_PATH.getParent();
			if (parentDir != null && !Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}
			Files.writeString(CACHE_PATH, GSON.toJson(KILL_COUNTS));
		} catch (IOException e) {
			LOGGER.error("[Skyblocker Party Counter] Failed to save cache", e);
		}
	}

	private static boolean isLocalPlayer(String name) {
		if (CLIENT.player == null) return false;
		return CLIENT.getUser().getName().equalsIgnoreCase(name);
	}

	private static boolean isConfirmedPartyMember(String playerName) {
		if (CLIENT.level == null) return false;
		for (var player : CLIENT.level.players()) {
			if (player.getGameProfile().name().equalsIgnoreCase(playerName)) {
				return PartyTracker.isPartyMember(player.getUUID());
			}
		}
		return false;
	}
}
