package de.hysky.skyblocker.stp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

public class SkyblockerTexturePredicates {
	private static final Logger LOGGER = LogUtils.getLogger();
	/**
	 * Turns a map of Skyblocker predicates into an array.
	 */
	@SuppressWarnings("unchecked")
	private static final Codec<Map<String, SkyblockerTexturePredicate>> DISPATCHED_MAP_CODEC = Codec.dispatchedMap(Codec.STRING, id -> (Codec<SkyblockerTexturePredicate>) SkyblockerPredicateType.REGISTRY.get(Identifier.of(id)).codec());
	public static final Codec<SkyblockerTexturePredicate[]> CODEC = DISPATCHED_MAP_CODEC
	.xmap(Map::values, SkyblockerTexturePredicates::values2Map)
	.xmap(collection -> collection.toArray(SkyblockerTexturePredicate[]::new), List::of);
	public static final Codec<List<Map<String, SkyblockerTexturePredicate>>> MAP_LIST_CODEC = DISPATCHED_MAP_CODEC.listOf();

	public static SkyblockerTexturePredicate[] compilePredicates(JsonObject overrides) {
		JsonObject predicate = overrides.getAsJsonObject("predicate");

		//There is model predicates defined
		if (predicate != null) {
			List<SkyblockerTexturePredicate> compiledPredicates = new ArrayList<>();

			try {
				for (Map.Entry<String, JsonElement> entry : predicate.asMap().entrySet()) {
					if (entry.getKey().startsWith("skyblocker")) {
						Identifier predicateId = Identifier.of(entry.getKey());

						if (SkyblockerPredicateType.REGISTRY.containsId(predicateId)) {
							try {
								Codec<? extends SkyblockerTexturePredicate> codec = SkyblockerPredicateType.REGISTRY.get(Identifier.of(entry.getKey())).codec();
								SkyblockerTexturePredicate compiledPredicate = codec.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow();

								compiledPredicates.add(compiledPredicate);
							} catch (Exception e) {
								LOGGER.error("[Skyblocker Predicate Compiler] Failed to compile prediate {}!", predicateId, e);
							}
						} else {
							LOGGER.warn("[Skyblocker Predicate Compiler] Encountered unknown predicate named {}, skipping.", predicateId);
						}
					}
				}

				if (!compiledPredicates.isEmpty()) return compiledPredicates.toArray(SkyblockerTexturePredicate[]::new);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Predicate Compiler] Failed to load predicates!", e);
			}
		}

		return null;
	}

	/**
	 * Flattening lists of predicate into a single array for logical predicates can significantly reduce
	 * allocation rates (of iterators) and improve performance (less time complexity).
	 */
	public static SkyblockerTexturePredicate[] flattenMap(List<Map<String, SkyblockerTexturePredicate>> predicateMaps) {
		return predicateMaps.stream()
				.map(Map::values)
				.flatMap(Collection::stream)
				.toArray(SkyblockerTexturePredicate[]::new);
	}

	private static Map<String, SkyblockerTexturePredicate> values2Map(Collection<SkyblockerTexturePredicate> predicates) {
		Map<String, SkyblockerTexturePredicate> map = new Object2ObjectOpenHashMap<>();

		for (SkyblockerTexturePredicate predicate : predicates) {
			map.put(SkyblockerPredicateType.REGISTRY.getEntry(predicate.getType()).getIdAsString(), predicate);
		}

		return map;
	}
}
