package de.hysky.skyblocker.config;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.azureaaron.dandelion.api.patching.ConfigPatch;
import net.minecraft.client.Minecraft;

public class ConfigPatchLoader {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(ConfigPatchLoader::fetchPatchList, 3600 * 20);
	}

	private static void fetchPatchList() {
		CompletableFuture.supplyAsync(() -> {
			try {
				String response = Http.sendGetRequest("https://api.azureaaron.net/skyblocker/configpatches");
				return JsonParser.parseString(response);
			} catch (Exception e) {
				LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to load config patches!", e);
				return null;
			}
		}, Executors.newVirtualThreadPerTaskExecutor())
		.thenAcceptAsync(json -> {
			List<ConfigPatch> patches = ConfigPatch.PATCH_LIST_CODEC.parse(JsonOps.INSTANCE, json)
					.setPartial(List.of())
					.resultOrPartial(error -> LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to parse config patches! Error: {}", error))
					.get();
			LOGGER.info("[Skyblocker Config Patch Loader] Successfully loaded config patches.");

			SkyblockerConfigManager.setPatches(patches);
		}, Minecraft.getInstance())
		.exceptionally(e -> {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to load config patches!", e);
			return null;
		});
	}
}
