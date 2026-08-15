package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
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

	/// {@return whether the member has unlocked the given tier of the collection}
	public static boolean unlockedTier(ProfileMember member, String collection, int tier) {
		IntList collectionTiers = tiers.getOrDefault(collection, IntList.of());
		long collected = member.collection.getOrDefault(collection, 0L);

		if (collectionTiers.size() >= tier) {
			int amountRequired = collectionTiers.getInt(tier - 1);

			return collected >= amountRequired;
		}

		return false;
	}

	/// {@return the highest tier of the given collection the member has unlocked}.
	public static int getMaxUnlockedTier(ProfileMember member, String collection) {
		IntList collectionTiers = tiers.getOrDefault(collection, IntList.of());
		long collected = member.collection.getOrDefault(collection, 0L);

		if (!collectionTiers.isEmpty()) {
			for (int i = collectionTiers.size(); i-- > 0;) {
				int amountRequired = collectionTiers.getInt(i);

				if (amountRequired <= collected) {
					return i - 1;
				}
			}
		}

		return 0;
	}

	/// {@return a mapping of collection categories to their items}
	///
	/// Example: FARMING -> [CARROT, POTATO]
	public static Map<String, List<String>> getCollectionCategoryContents() {
		return categories;
	}

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

				// Store the items this collection type has (e.g. all FARMING items)
				parsedCategories.put(typeName, List.copyOf(items.keySet()));

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
			}

			tiers = Map.copyOf(parsedTiers);
			categories = Map.copyOf(parsedCategories);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to parse collections data.", e);
		}
	}
}
