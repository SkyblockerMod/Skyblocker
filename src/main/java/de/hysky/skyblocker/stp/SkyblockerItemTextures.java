package de.hysky.skyblocker.stp;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier.OnLoad;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class SkyblockerItemTextures {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String ITEM_MODELS_FOLDER = "item";
	private static final Object2ObjectMap<String, Identifier> ID_2_MODEL_ID = new Object2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		PreparableModelLoadingPlugin.register(SkyblockerItemTextures::prepareItemModels, (data, pluginContext) -> {
			pluginContext.addModels(data);
			pluginContext.modifyModelOnLoad().register(SkyblockerItemTextures::onModelLoad);
		});
	}

	private static CompletableFuture<Collection<Identifier>> prepareItemModels(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				ID_2_MODEL_ID.clear();

				Map<String, Identifier> ids2ModelIds = new Object2ObjectOpenHashMap<>();
				Set<Identifier> modelsToBake = new ObjectOpenHashSet<>();
				Map<Identifier, Resource> modelFiles = manager.findResources(ModelHelper.MODELS_PREFIX + ITEM_MODELS_FOLDER, ModelHelper::isValidJsonPath);
				Set<Identifier> authoritativeModels = ModelHelper.findAuthoritativeModels(modelFiles);

				//FIXME remove
				LOGGER.info("[Skyblocker Item Textures Debug] Authoritative Models: {}", authoritativeModels);

				for (Identifier id : modelFiles.keySet()) {
					Identifier normalized = ModelHelper.normalizeModelId(id);
					Matcher matcher = ModelHelper.FILE_NAME_PATTERN.matcher(id.getPath());

					//Ignore models which are not "authoritative" to avoid storing potentially hundreds (or thousands!) of useless model references.
					if (authoritativeModels == null || authoritativeModels.contains(normalized)) {
						if (matcher.find()) {
							ids2ModelIds.put(matcher.group("name").toUpperCase(Locale.ENGLISH), normalized);
						} else {
							LOGGER.error("[Skyblocker Item Textures] Could not find name for id {}.", id);
						}
					}

					modelsToBake.add(normalized);
				}

				ID_2_MODEL_ID.putAll(ids2ModelIds);

				return modelsToBake;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Item Textures] Failed to prepare item models for baking!", e);
			}

			return Set.of();
		}, executor);
	}

	private static UnbakedModel onModelLoad(UnbakedModel model, OnLoad.Context context) {
		ModelHelper.applyTextureSugar(model, context.resourceId());

		return model;
	}

	public static Identifier getModelId(String skyblockId) {
		return ID_2_MODEL_ID.get(skyblockId);
	}
}
