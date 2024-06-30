package de.hysky.skyblocker.stp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import net.minecraft.util.Identifier;

public class SkyblockerTexturePredicates {
	private static final Logger LOGGER = LogUtils.getLogger();

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
}
