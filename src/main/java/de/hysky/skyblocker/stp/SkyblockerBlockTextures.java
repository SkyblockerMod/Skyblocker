package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.predicates.LocationPredicate;
import de.hysky.skyblocker.stp.predicates.SkyblockerTexturePredicate;
import de.hysky.skyblocker.utils.datafixer.ItemStackComponentizationFixer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class SkyblockerBlockTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final String MODELS_PREFIX = "models/";
	private static final String MODELS_FOLDER_PATH = "block";
	private static final String BLOCK_OVERRIDES_PATH = "overrides/block";
	private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<name>[A-Za-z0-9_\\- ]+)(?=.json)");
	private static final Set<CustomBlockOverride> CUSTOM_BLOCK_OVERRIDES = new ObjectOpenHashSet<>();

	private static CustomBlockOverride lastOverride = null;

	public static void init() {
		PreparableModelLoadingPlugin.register(SkyblockerBlockTextures::prepareBlockModels, (data, pluginContext) -> {
			pluginContext.addModels(data); //Load & Bake the prepared block models
		});
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Identifier.of(SkyblockerMod.NAMESPACE, "custom_block_textures");
			}

			@Override
			public void reload(ResourceManager manager) {
				loadCustomBlockTextureDefinitions(manager);
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	private static CompletableFuture<Set<Identifier>> prepareBlockModels(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Set<Identifier> modelFileIds = new ObjectOpenHashSet<>();
				Map<Identifier, Resource> modelFiles = manager.findResources(MODELS_PREFIX + MODELS_FOLDER_PATH, id -> id.getNamespace().equals(SkyblockerMod.NAMESPACE) && id.getPath().endsWith(".json"));

				for (Identifier id : modelFiles.keySet()) {
					String path = id.getPath();
					Identifier formatted = Identifier.of(SkyblockerMod.NAMESPACE, path.replaceFirst(MODELS_PREFIX, "").replace(".json", ""));

					modelFileIds.add(formatted);
				}

				return modelFileIds;
			} catch (Throwable t) {
				LOGGER.error("[Skyblocker Block Textures] Failed to prepare block models for baking!", t);
			}

			return Set.<Identifier>of();
		}, executor);
	}

	private static void loadCustomBlockTextureDefinitions(ResourceManager manager) {
		try {
			CUSTOM_BLOCK_OVERRIDES.clear();
			Map<Identifier, Resource> blockOverrides = manager.findResources(BLOCK_OVERRIDES_PATH, id -> id.getNamespace().equals(SkyblockerMod.NAMESPACE) && id.getPath().endsWith(".json"));
			Set<CompletableFuture<List<CustomBlockOverride>>> futures = new ObjectOpenHashSet<>();

			for (Map.Entry<Identifier, Resource> entry : blockOverrides.entrySet()) {
				CompletableFuture<List<CustomBlockOverride>> future = computeBlockOverride(entry);

				futures.add(future);
			}

			//Block until all finished
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

			for (CompletableFuture<List<CustomBlockOverride>> future : futures) {
				List<CustomBlockOverride> overrides = future.get();

				if (overrides != null) {
					CUSTOM_BLOCK_OVERRIDES.addAll(overrides);
				}
			}
		} catch (Throwable t) {
			LOGGER.error("[Skyblocker Block Textures] Failed to load custom block texture rules!", t);
		}
	}

	private static CompletableFuture<List<CustomBlockOverride>> computeBlockOverride(Map.Entry<Identifier, Resource> entry) {
		return CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = entry.getValue().getReader()) {
				JsonArray file = JsonParser.parseReader(reader).getAsJsonArray();

				return CustomBlockOverride.ofList(entry.getKey().getPath(), file);
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Block Textures] Failed to load regions from file {} :(", entry.getKey(), e);
			}

			return null;
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	public static Identifier getBlockReplacement(Block block) {
		overrideLoop: for (CustomBlockOverride override : CUSTOM_BLOCK_OVERRIDES) {
			if (override.predicates() != null) {
				for (SkyblockerTexturePredicate predicate : override.predicates()) {
					if (!predicate.test(null)) continue overrideLoop;
				}

				CustomBlockOverride newOverride = override;

				if (newOverride != lastOverride && CLIENT.worldRenderer != null) {
					Scheduler.INSTANCE.schedule(CLIENT.worldRenderer::reload, 1);
					lastOverride = newOverride;
				}

				return newOverride.replacements().get(block);
			}
		}

		return null;
	}

	private static void reset() {
		lastOverride = null;
	}

	public record CustomBlockOverride(Map<Block, Identifier> replacements, SkyblockerTexturePredicate[] predicates) {
		private static List<CustomBlockOverride> ofList(String fileName, JsonArray overrides) {

			//Synthesize a location predicate from the file's name is possible;
			LocationPredicate locationPredicate = null;
			try {
				Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
				matcher.find();

				locationPredicate = LocationPredicate.CODEC.parse(JavaOps.INSTANCE, matcher.group("name")).getOrThrow();
			} catch (Throwable ignored) {}

			LocationPredicate locationPredicate2 = locationPredicate; //I hate the final/effectively final restrictions with lambdas

			return overrides.asList().stream()
					.map(JsonElement::getAsJsonObject)
					.map(obj -> CustomBlockOverride.of(obj, locationPredicate2))
					.toList();
		}

		private static CustomBlockOverride of(JsonObject override, LocationPredicate locationPredicate) {
			Map<Block, Identifier> replacements = Codec.unboundedMap(Identifier.CODEC.xmap(Registries.BLOCK::get, Registries.BLOCK::getId), Identifier.CODEC.xmap(id -> Identifier.of(SkyblockerMod.NAMESPACE, id.getPath()), Function.identity()))
					.xmap(Object2ObjectOpenHashMap::new, Object2ObjectOpenHashMap::new)
					.parse(ItemStackComponentizationFixer.getRegistryLookup().getOps(JsonOps.INSTANCE), override.get("replacements")).getOrThrow();

			SkyblockerTexturePredicate[] compiledPredicates = SkyblockerTexturePredicates.compilePredicates(override);
			List<SkyblockerTexturePredicate> filteredPredicates = compiledPredicates == null ? List.of() : Arrays.stream(compiledPredicates)
					.filter(p -> !p.itemStackDependent())
					.toList();

			SkyblockerTexturePredicate[] finalPredicates = Util.make(new ArrayList<>(), list -> {
				//Copy filtered predicates to the list
				list.addAll(filteredPredicates);

				//Add the inferred location predicate to the list if it was present
				if (locationPredicate != null) list.add(locationPredicate);
			}).toArray(SkyblockerTexturePredicate[]::new);

			return new CustomBlockOverride(replacements, finalPredicates.length > 0 ? finalPredicates : null);
		}
	}
}
