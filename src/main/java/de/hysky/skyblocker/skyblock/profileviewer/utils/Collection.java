package de.hysky.skyblocker.skyblock.profileviewer.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public record Collection(String id, String name, IntList tiers) {
	private static final Decoder<IntList> TIERS_DECODER = Codec.PASSTHROUGH.map(tiersDynamic -> IntLists.unmodifiable(tiersDynamic.asStream()
			.<Number>mapMulti((tierDynamic, consumer) -> tierDynamic.get("amountRequired").asNumber().ifSuccess(consumer))
			.mapToInt(Number::intValue)
			.collect(IntArrayList::of, IntList::add, IntList::addAll)));

	public static Map<String, List<Collection>> parse(JsonObject object) {
		if (!object.get("success").getAsBoolean()) return Map.of();
		Map<String, List<Collection>> skillCollections = new HashMap<>();

		for (Map.Entry<String, JsonElement> skillEntry : object.getAsJsonObject("collections").asMap().entrySet()) {
			List<Collection> collections = new ArrayList<>();

			for (Map.Entry<String, JsonElement> collectionEntry : skillEntry.getValue().getAsJsonObject().getAsJsonObject("items").asMap().entrySet()) {
				String id = collectionEntry.getKey();
				String name = collectionEntry.getValue().getAsJsonObject().get("name").getAsString();
				IntList tiers = TIERS_DECODER.parse(JsonOps.INSTANCE,  collectionEntry.getValue().getAsJsonObject().get("tiers")).getOrThrow();
				Collection collection = new Collection(id, name, tiers);

				collections.add(collection);
			}

			// Sort collections by name
			collections.sort(Comparator.comparing(Collection::name, String::compareToIgnoreCase));
			skillCollections.put(skillEntry.getKey(), List.copyOf(collections));
		}

		return Map.copyOf(skillCollections);
	}
}
