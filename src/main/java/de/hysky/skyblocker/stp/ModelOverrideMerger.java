package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.SkyblockerMod;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ModelOverrideMerger {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String ITEM_OVERRIDES_PATH = "overrides/item";
	private static final ConcurrentHashMap<Identifier, JsonArray> MODEL_OVERRIDES_TO_BE_MERGED = new ConcurrentHashMap<>();

	/**
	 * Loads all model overrides from our files and then stores them in a map so that they can be merged with their vanilla counterpart
	 * later on in the model loading process.
	 */
	public static void compileOverrides(ResourceManager manager) {
		try {
			MODEL_OVERRIDES_TO_BE_MERGED.clear();
			Map<Identifier, Resource> itemModelOverrides = manager.findResources(ITEM_OVERRIDES_PATH, id -> id.getNamespace().equals(SkyblockerMod.NAMESPACE) && id.getPath().endsWith(".json"));
			List<CompletableFuture<Pair<Identifier, JsonArray>>> futures = new ArrayList<>();

			for (Map.Entry<Identifier, Resource> entry : itemModelOverrides.entrySet()) {
				CompletableFuture<Pair<Identifier, JsonArray>> future = computeOverride(entry);

				futures.add(future);
			}

			//Block until its all completed
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

			//If the parent model isn't in the array then store the overrides, otherwise merge them with the existing ones
			for (CompletableFuture<Pair<Identifier, JsonArray>> future : futures) {
				Pair<Identifier, JsonArray> pair = future.get();

				if (pair == null) continue;

				Identifier parent = pair.left();
				JsonArray overrides = pair.right();

				if (!MODEL_OVERRIDES_TO_BE_MERGED.containsKey(parent)) {
					MODEL_OVERRIDES_TO_BE_MERGED.put(parent, overrides);
				} else {
					JsonArray existingOverrides = MODEL_OVERRIDES_TO_BE_MERGED.get(parent);

					existingOverrides.addAll(overrides);
				}
			}
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Model Override Merger] Failed to compile model overrides to be merged!", t);
		}
	}

	/**
	 * Loads each item override inside of a virtual thread for maximum efficiency, should improve performance when
	 * there is a lot of item model override files that need loading.
	 */
	private static CompletableFuture<Pair<Identifier, JsonArray>> computeOverride(Map.Entry<Identifier, Resource> entry) {
		return CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
				Identifier parentModel = Identifier.ofVanilla("models/item/" + file.get("parent").getAsString() + ".json");
				JsonArray overrides = file.getAsJsonArray("overrides");

				return Pair.of(parentModel, overrides);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Model Override Merger] Failed to load model overrides from {}.", entry.getKey(), e);
			}

			return null;
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static JsonArray getOverrides(Identifier id) {
		if (MODEL_OVERRIDES_TO_BE_MERGED.containsKey(id)) {
			return MODEL_OVERRIDES_TO_BE_MERGED.get(id);
		}

		return null;
	}

	public static Reader tryMerge(Reader original, Identifier id, Resource resource) throws IOException {
		JsonArray overrides2Merge = getOverrides(id);

		if (overrides2Merge != null) {
			try (original) {
				JsonObject modelFile = JsonParser.parseReader(original).getAsJsonObject();

				if (modelFile.has("overrides")) {
					JsonArray originalOverrides = modelFile.getAsJsonArray("overrides");

					originalOverrides.addAll(overrides2Merge);
				} else {
					modelFile.add("overrides", overrides2Merge);
				}

				String encoded = SkyblockerMod.GSON_COMPACT.toJson(modelFile);

				return new StringReader(encoded);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Model Override Merger] Failed to merge overrides for model {}.", id, e);
			}

			//Return a new reader, if our parsing above fails vanilla's likely would too but we will return a new reader just in case.
			return resource.getReader();
		}

		return original;
	}
}
