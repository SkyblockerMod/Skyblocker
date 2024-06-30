package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.MultithreadedTaskHelper;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceObjectPair;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SkyblockerBlockTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final String BLOCK_MODELS_FOLDER = "block";
	private static final String BLOCK_OVERRIDES_PATH = "overrides/block";
	private static final Reference2ObjectMap<Location, CustomBlockOverride> CUSTOM_BLOCK_OVERRIDES = new Reference2ObjectOpenHashMap<>();

	private static Reference2ObjectMap<Block, Identifier> lastReplacements = null;

	@Init
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
				reset();
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	private static CompletableFuture<Set<Identifier>> prepareBlockModels(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Set<Identifier> modelFileIds = new ObjectOpenHashSet<>();
				Map<Identifier, Resource> modelFiles = manager.findResources(ModelHelper.MODELS_PREFIX + BLOCK_MODELS_FOLDER, ModelHelper::isValidJsonPath);

				for (Identifier id : modelFiles.keySet()) {
					modelFileIds.add(ModelHelper.normalizeModelId(id));
				}

				return modelFileIds;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Block Textures] Failed to prepare block models for baking!", e);
			}

			return Set.of();
		}, executor);
	}

	private static void loadCustomBlockTextureDefinitions(ResourceManager manager) {
		try {
			CUSTOM_BLOCK_OVERRIDES.clear();
			Map<Identifier, Resource> blockOverrides = manager.findResources(BLOCK_OVERRIDES_PATH, id -> id.getNamespace().equals(SkyblockerMod.NAMESPACE) && id.getPath().endsWith(".json"));
			MultithreadedTaskHelper<ReferenceObjectPair<Location, CustomBlockOverride>> taskHelper = MultithreadedTaskHelper.create();

			for (Map.Entry<Identifier, Resource> entry : blockOverrides.entrySet()) {
				taskHelper.addTask(() -> compileBlockOverride(entry.getKey(), entry.getValue()), Executors.newVirtualThreadPerTaskExecutor());
			}

			//Block until all finished
			taskHelper.complete(override -> {
				if (override != null) CUSTOM_BLOCK_OVERRIDES.put(override.left(), override.right());
			});
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Block Textures] Failed to load custom block texture rules!", e);
		}
	}

	private static ReferenceObjectPair<Location, CustomBlockOverride> compileBlockOverride(Identifier id, Resource resource) {
		try (BufferedReader reader = resource.getReader()) {
			Matcher matcher = ModelHelper.FILE_NAME_PATTERN.matcher(id.toString());

			if (matcher.find()) {
				JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
				Location location = Location.from(matcher.group("name"));

				if (location == Location.UNKNOWN) {
					LOGGER.error("[Skyblocker Block Textures] Read unknown location: {}. Double-check that the file name is correct.", matcher.group("name"));

					return null;
				}

				CustomBlockOverride override = CustomBlockOverride.CODEC.parse(JsonOps.INSTANCE, file).getOrThrow();

				return ReferenceObjectPair.of(location, override);
			} else {
				LOGGER.error("[Skyblocker Block Textures] Couldn't parse file name! Name: {}", id);
			}

			return null;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Block Textures] Failed to load regions from file {} :(", id, e);
		}

		return null;
	}

	public static Identifier getBlockReplacement(Block block, @Nullable BlockPos pos) {
		if (CUSTOM_BLOCK_OVERRIDES.containsKey(Utils.getLocation())) {
			CustomBlockOverride override = CUSTOM_BLOCK_OVERRIDES.get(Utils.getLocation());
			BlockPos testPos = pos != null ? pos : CLIENT.player.getBlockPos();

			for (CustomBlockOverride.CustomBlockSubOverride subOverride : override.overrides()) {
				if (subOverride.box().contains(testPos.getX(), testPos.getY(), testPos.getZ())) {
					Reference2ObjectMap<Block, Identifier> replacements = subOverride.replacements();
					updateActiveReplacements(replacements);

					return replacements.get(block);
				}
			}

			if (override.box().isEmpty() || (override.box().isPresent() && override.box().get().contains(testPos.getX(), testPos.getY(), testPos.getZ()))) {
				Reference2ObjectMap<Block, Identifier> replacements = override.replacements();
				updateActiveReplacements(replacements);

				return replacements.get(block);
			}
		}

		return null;
	}

	private static void updateActiveReplacements(Reference2ObjectMap<Block, Identifier> replacements) {
		if (replacements != lastReplacements) {
			Scheduler.INSTANCE.schedule(CLIENT.worldRenderer::reload, 1);
			lastReplacements = replacements;
		}
	}

	private static void reset() {
		lastReplacements = null;
	}

	private record CustomBlockOverride(Reference2ObjectMap<Block, Identifier> replacements, Optional<Box> box, CustomBlockSubOverride[] overrides) {
		private static final Codec<Reference2ObjectMap<Block, Identifier>> REPLACEMENTS_CODEC = Codec.unboundedMap(Identifier.CODEC.xmap(Registries.BLOCK::get, Registries.BLOCK::getId), Identifier.CODEC)
				.xmap(Reference2ObjectOpenHashMap::new, Reference2ObjectOpenHashMap::new);
		private static final Codec<Box> BOX_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				BlockPos.CODEC.fieldOf("pos1").forGetter(box -> BlockPos.ofFloored(box.getMinPos())),
				BlockPos.CODEC.fieldOf("pos2").forGetter(box -> BlockPos.ofFloored(box.getMaxPos())))
				.apply(instance, Box::enclosing));
		private static final Codec<CustomBlockOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				REPLACEMENTS_CODEC.fieldOf("replacements").forGetter(CustomBlockOverride::replacements),
				BOX_CODEC.optionalFieldOf("box").forGetter(CustomBlockOverride::box),
				CustomBlockSubOverride.CODEC.listOf().xmap(list -> list.toArray(CustomBlockSubOverride[]::new), List::of).optionalFieldOf("overrides", new CustomBlockSubOverride[0]).forGetter(CustomBlockOverride::overrides))
				.apply(instance, CustomBlockOverride::new));

		private record CustomBlockSubOverride(Reference2ObjectMap<Block, Identifier> replacements, Box box) {
			private static final Codec<CustomBlockSubOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					REPLACEMENTS_CODEC.fieldOf("replacements").forGetter(CustomBlockSubOverride::replacements),
					BOX_CODEC.fieldOf("box").forGetter(CustomBlockSubOverride::box))
					.apply(instance, CustomBlockSubOverride::new));
		}
	}
}
