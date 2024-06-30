package de.hysky.skyblocker.stp;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.accessors.JsonUnbakedModelAccessor;
import de.hysky.skyblocker.utils.MultithreadedTaskHelper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

/**
 * Helper class to assist with handling model files.
 */
public class ModelHelper {
	private static final Logger LOGGER = LogUtils.getLogger();
	static final String MODELS_PREFIX = "models/";
	static final Pattern FILE_NAME_PATTERN = Pattern.compile("(?<name>[A-Za-z0-9_\\-]+)(?=.json)");
	static final Pattern FILE_PATH_PATTERN = Pattern.compile("(?<path>[\\/A-Za-z0-9-_]*\\/)(?=[A-Za-z0-9\\-_])");

	/**
	 * Returns true if the {@code id}'s namespace is {@code skyblocker} and the path ends with {@code .json}.
	 */
	static boolean isValidJsonPath(Identifier id) {
		return id.getNamespace().equals(SkyblockerMod.NAMESPACE) && id.getPath().endsWith(".json");
	}

	/**
	 * Removes the "model/" prefix and {@code .json} extension from {@code Identifiers} the paths of JSON model files.
	 */
	static Identifier normalizeModelId(Identifier original) {
		return Identifier.of(original.getNamespace(), original.getPath().replaceFirst(MODELS_PREFIX, "").replace(".json", ""));
	}

	/**
	 * Determine the "authoritative" models (which weren't referenced by an override).
	 */
	@Nullable
	static Set<Identifier> findAuthoritativeModels(Map<Identifier, Resource> models) {
		try {
			MultithreadedTaskHelper<Set<Identifier>> taskHelper = MultithreadedTaskHelper.create();

			for (Map.Entry<Identifier, Resource> model : models.entrySet()) {
				taskHelper.addTask(() -> findReferencedModels(model.getKey(), model.getValue()), Executors.newVirtualThreadPerTaskExecutor());
			}

			Set<Identifier> referencedModels = new ObjectOpenHashSet<>();

			taskHelper.complete(referencedModelsInOverrides -> {
				if (referencedModelsInOverrides != null) referencedModels.addAll(referencedModelsInOverrides);
			});

			Set<Identifier> authoritativeModels = models.keySet().stream()
					.map(ModelHelper::normalizeModelId)
					.filter(Predicate.not(referencedModels::contains))
					.collect(Collectors.toUnmodifiableSet());

			return authoritativeModels;
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Model Helper] Failed to find authoritative models!", e);
		}

		return null;
	}

	@Nullable
	private static Set<Identifier> findReferencedModels(Identifier file, Resource resource) {
		try (BufferedReader reader = resource.getReader()) {
				JsonObject modelFile = JsonParser.parseReader(reader).getAsJsonObject();

				if (modelFile.has("overrides")) {
					Set<Identifier> referencedModels = modelFile.getAsJsonArray("overrides").asList().stream()
							.map(JsonElement::getAsJsonObject)
							.filter(override -> override.has("model"))
							.map(override -> override.get("model").getAsString())
							.map(Identifier::of)
							.collect(Collectors.toUnmodifiableSet());

					return referencedModels;
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Model Helper] Failed to find refernced models for: {}", file, e);
			}
			return null;
	}

	/**
	 * Allows for special syntax to automatically resolve of texture layer paths to ease porting pains and reduce boilerplate.
	 */
	static void applyTextureSugar(UnbakedModel model, Identifier modelId) {
		if (modelId != null && modelId.getNamespace().equals(SkyblockerMod.NAMESPACE) && (modelId.getPath().startsWith("item/") || modelId.getPath().startsWith("universal/")) && model instanceof JsonUnbakedModelAccessor jsonModel) {
			for (Map.Entry<String, Either<SpriteIdentifier, String>> entry : jsonModel.getTextureMap().entrySet()) {
				Optional<SpriteIdentifier> spriteId = entry.getValue().left();

				if (spriteId.isPresent()) {
					Identifier tex = spriteId.get().getTextureId();
					String modelIdPath = modelId.getPath();
					
					if (modelIdPath.startsWith("universal/")) {
						modelIdPath = modelIdPath.replaceFirst("universal/", "item/universal/");
					}

					if (tex.getNamespace().equals(SkyblockerMod.NAMESPACE)) {
						if (tex.getPath().startsWith("relative/")) {
							Matcher matcher = FILE_PATH_PATTERN.matcher(modelIdPath);

							if (matcher.find()) {
								Identifier newTexturePath = Identifier.of(SkyblockerMod.NAMESPACE, tex.getPath().replace("relative/", matcher.group("path")));
								SpriteIdentifier newSpriteId = new SpriteIdentifier(spriteId.get().getAtlasId(), newTexturePath);

								entry.setValue(Either.left(newSpriteId));
							} else {
								LOGGER.error("[Skyblocker Item Textures] Failed to match path for model id {}", modelId);
							}
						} else if (tex.getPath().equals("exact")) {
							SpriteIdentifier newSpriteId = new SpriteIdentifier(spriteId.get().getAtlasId(), modelId.withPath(modelIdPath));

							entry.setValue(Either.left(newSpriteId));
						}
					}
				}
			}
		}
	}
}
