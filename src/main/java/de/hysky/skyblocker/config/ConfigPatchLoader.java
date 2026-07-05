package de.hysky.skyblocker.config;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.azureaaron.dandelion.api.patching.ConfigPatch;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ConfigPatchLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	/// Update every half hour (in ticks).
	private static final int UPDATE_INTERVAL = 1800 * 20;

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(ConfigPatchLoader::fetchPatchList, UPDATE_INTERVAL);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(ClientCommands.literal(SkyblockerMod.NAMESPACE)
				.then(ClientCommands.literal("updateConfigPatches")
						.executes(context -> {
							Minecraft minecraft = context.getSource().getClient();
							fetchPatchList().thenAcceptAsync(result -> {
								if (minecraft.player != null) {
									if (result) {
										minecraft.player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.patches.updated.success")));
									} else {
										minecraft.player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.patches.updated.failed")));
									}
								}
							}, minecraft);

							return Command.SINGLE_SUCCESS;
						}))
				));
	}

	private static CompletableFuture<Boolean> fetchPatchList() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				String response = Http.sendGetRequest("https://api.azureaaron.net/skyblocker/configpatches");
				return JsonParser.parseString(response);
			} catch (Exception e) {
				LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to load config patches!", e);
				return null;
			}
		}, Executors.newVirtualThreadPerTaskExecutor())
		.thenApplyAsync(json -> {
			List<ConfigPatch> patches = ConfigPatch.PATCH_LIST_CODEC.parse(JsonOps.INSTANCE, json)
					.setPartial(List.of())
					.resultOrPartial(error -> LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to parse config patches! Error: {}", error))
					.get();
			LOGGER.info("[Skyblocker Config Patch Loader] Successfully loaded config patches.");

			SkyblockerConfigManager.setPatches(patches);

			return true;
		}, Minecraft.getInstance())
		.exceptionally(e -> {
			LOGGER.error(LogUtils.FATAL_MARKER, "[Skyblocker Config Patch Loader] Failed to load config patches!", e);
			return false;
		});
	}
}
