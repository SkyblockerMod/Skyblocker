package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.Minecraft;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.utils.Http;

public class CollectionTiers {
	private static final Logger LOGGER = LogUtils.getLogger();
	/// Mapping of collection to its tiers.
	///
	/// Example: POTATO -> [100, 200, ...]
	private static Map<String, IntList> tiers = Map.of();
	/// Mapping of collection categories to their items.
	///
	/// Example: FARMING -> [CARROT, POTATO]
	private static Map<String, List<String>> categories = Map.of();

	@Init
	public static void init() {
		CompletableFuture.supplyAsync(CollectionTiers::loadCollectionsData, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR)
		.thenAcceptAsync(CollectionTiers::parseCollectionsData, Minecraft.getInstance());
	}

	/// {@return whether the profile has unlocked the given tier of the collection}
	public static boolean unlockedTier(ApiProfile profile, String collection, int tier) {
		return getMaxUnlockedTier(profile, collection) >= tier;
	}

	/// {@return the highest tier of the given collection the profile has unlocked}.
	public static int getMaxUnlockedTier(ApiProfile profile, String collection) {
		long collected = profile.members.entrySet().stream()
				.map(Map.Entry::getValue)
				.mapToLong(member -> member.collection.getOrDefault(collection, 0L))
				.sum();

		return getMaxUnlockedTier(collection, collected);
	}

	private static int getMaxUnlockedTier(String collection, long collected) {
		IntList collectionTiers = tiers.getOrDefault(collection, IntList.of());

		if (!collectionTiers.isEmpty()) {
			for (int i = collectionTiers.size() - 1; i >= 0; i--) {
				int amountRequired = collectionTiers.getInt(i);

				if (amountRequired <= collected) {
					return i + 1;
				}
			}
		}

		return 0;
	}

	/// {@return the highest tier of the given collection}
	public static int getMaxTier(String collection) {
		return tiers.getOrDefault(collection, IntList.of()).size();
	}

	/// {@return a mapping of collection categories to their items}
	///
	/// Example: FARMING -> [CARROT, POTATO]
	public static Map<String, List<String>> getCollectionCategoryContents() {
		return categories;
	}

	/// {@return a report of the collection for the current member & profile}
	public static Report getCollectionReport(ApiProfile profile, UUID member, String collection) {
		long personal = profile.members.get(member).collection.getOrDefault(collection, 0L);
		long coop = profile.members.entrySet().stream()
				.filter(entry -> !entry.getKey().equals(member))
				.map(Map.Entry::getValue)
				.mapToLong(profileMember -> profileMember.collection.getOrDefault(collection, 0L))
				.sum();
		long total = personal + coop;
		int tier = getMaxUnlockedTier(collection, total);

		return new Report(personal, coop, total, tier);
	}

	public record Report(long personal, long coop, long total, int tier) {}

	private static @Nullable JsonElement loadCollectionsData() {
		try {
			String response = Http.sendGetRequest("https://api.hypixel.net/v2/resources/skyblock/collections");

			return JsonParser.parseString(response);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to load collections data.", e);

			return null;
		}
	}

	private static void parseCollectionsData(@Nullable JsonElement data) {
		// The data failed to load sadly
		if (data == null) {
			return;
		}

		try {
			Map<String, IntList> parsedTiers = new HashMap<>();
			Map<String, List<String>> parsedCategories = new HashMap<>();

			JsonObject collections = data.getAsJsonObject().getAsJsonObject("collections");

			for (Map.Entry<String, JsonElement> typeEntry : collections.entrySet()) {
				String typeName = typeEntry.getKey();
				JsonObject items = typeEntry.getValue().getAsJsonObject().getAsJsonObject("items");

				for (Map.Entry<String, JsonElement> itemsEntry : items.entrySet()) {
					String itemName = itemsEntry.getKey();
					JsonObject item = itemsEntry.getValue().getAsJsonObject();
					int[] itemTiers = item.getAsJsonArray("tiers").asList().stream()
							.map(JsonElement::getAsJsonObject)
							// Sort based on the tier, never know if one day the tiers will end up unordered
							.sorted((o1, o2) -> Integer.compare(o1.get("tier").getAsInt(), o2.get("tier").getAsInt()))
							.map(tier -> tier.get("amountRequired").getAsInt())
							.mapToInt(Integer::intValue)
							.toArray();

					parsedTiers.put(itemName, IntList.of(itemTiers));
				}

				// Store the items this collection type has (e.g. all FARMING items) alphabetically
				List<String> collectionItems = items.entrySet().stream()
						.sorted((o1, o2) -> {
							String name1 = o1.getValue().getAsJsonObject().get("name").getAsString();
							String name2 = o2.getValue().getAsJsonObject().get("name").getAsString();

							return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
						})
						.map(Map.Entry::getKey)
						.toList();
				parsedCategories.put(typeName, collectionItems);
			}

			tiers = Map.copyOf(parsedTiers);
			categories = Map.copyOf(parsedCategories);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to parse collections data.", e);
		}
	}
}
