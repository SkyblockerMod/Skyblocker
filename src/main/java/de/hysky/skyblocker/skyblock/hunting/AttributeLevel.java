package de.hysky.skyblocker.skyblock.hunting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains a few utilities for handling attribute level data.
 */
public class AttributeLevel {
	private static final Identifier ATTRIBUTE_UPGRADES_FILE = Identifier.of(SkyblockerMod.NAMESPACE, "hunting/attribute_upgrades.json");
	private static final Map<SkyblockItemRarity, Int2IntMap> SHARD_LEVELS = new EnumMap<>(SkyblockItemRarity.class);
	public static final int MAX_LEVEL = 10;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(AttributeLevel::loadUpgrades);
	}

	/**
	 * Returns the cumulative shard count required for a given rarity and level.
	 *
	 * @param rarity The rarity of the item.
	 * @param level The level of the upgrade (1-indexed).
	 * @return The cumulative shard count required for the given rarity and level, or -1 if not found.
	 */
	public static int getCumulativeShardCount(SkyblockItemRarity rarity, int level) {
		Int2IntMap level2CountMap = SHARD_LEVELS.get(rarity);
		if (level2CountMap == null) return -1;
		return level2CountMap.getOrDefault(level, -1);
	}

	/**
	 * Returns the maximum shard count required for a given rarity.
	 *
	 * @param rarity The rarity of the item.
	 * @return The maximum shard count required for the given rarity, or -1 if not found.
	 */
	public static int getMaxShardCount(SkyblockItemRarity rarity) {
		return getCumulativeShardCount(rarity, MAX_LEVEL);
	}

	/**
	 * Returns the number of shards needed to reach the maximum level for a given rarity and current level.
	 *
	 * @param rarity The rarity of the item.
	 * @param level The current level of the upgrade (1-indexed).
	 * @return The number of shards needed to reach the maximum level, or -1 if not found or if the level is invalid.
	 */
	public static int getShardsUntilMax(SkyblockItemRarity rarity, int level) {
		if (level == MAX_LEVEL) return 0; // Already at max level, we can just skip.

		Int2IntMap level2CountMap = SHARD_LEVELS.get(rarity);
		if (level2CountMap == null) return -1;

		int currentShardCount = level2CountMap.getOrDefault(level, -1);
		if (currentShardCount == -1) return -1;

		int maxShardCount = level2CountMap.getOrDefault(MAX_LEVEL, -1);
		if (maxShardCount == -1) return -1;

		return maxShardCount - currentShardCount;
	}

	private static void loadUpgrades(MinecraftClient client) {
		// We want it to crash and burn if it's not as expected, so we don't explicitly handle nulls or incorrect types. This resource is bundled in the mod, so it should always be present and the same.
		try (BufferedReader reader = client.getResourceManager().openAsReader(ATTRIBUTE_UPGRADES_FILE)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			for (Entry<String, JsonElement> entry : json.entrySet()) {
				String rarity = entry.getKey();
				SkyblockItemRarity skyblockItemRarity = SkyblockItemRarity.valueOf(rarity.toUpperCase(Locale.ROOT));

				Int2IntMap level2CountMap = SHARD_LEVELS.computeIfAbsent(skyblockItemRarity, key -> new Int2IntArrayMap());
				JsonArray levels = entry.getValue().getAsJsonObject().getAsJsonArray("shardsForUpgrade"); // The same goes here
				Iterator<JsonElement> iterator = levels.iterator();
				int i = 1; // Levels are 1-indexed, so we start at 1
				while (iterator.hasNext()) {
					int shardCount = iterator.next().getAsInt();
					level2CountMap.put(i++, shardCount);
				}
				if (i <= MAX_LEVEL) throw new IllegalStateException("Shard levels for rarity " + rarity + " are incomplete. Expected at least 10 levels, but found only " + (i - 1) + ".");
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load shard levels", e);
		}
	}
}
