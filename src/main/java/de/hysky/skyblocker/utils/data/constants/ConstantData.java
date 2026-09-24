package de.hysky.skyblocker.utils.data.constants;

import java.io.BufferedReader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;

public final class ConstantData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static EmblemConstants emblemConstants = EmblemConstants.EMPTY;
	private static MinionConstants minionConstants = MinionConstants.EMPTY;

	private ConstantData() {}

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(minecraft -> {
			loadDataAsync(minecraft, "emblems.json", EmblemConstants.CODEC, data -> emblemConstants = data);
			loadDataAsync(minecraft, "minions.json", MinionConstants.CODEC, data -> minionConstants = data);
		});
	}

	public static EmblemConstants getEmblemConstants() {
		return emblemConstants;
	}

	public static MinionConstants getMinionConstants() {
		return minionConstants;
	}

	private static <T> void loadDataAsync(Minecraft minecraft, String fileName, Codec<T> codec, Consumer<T> setter) {
		CompletableFuture.supplyAsync(() -> loadData(minecraft, fileName, codec), SkyblockerMod.VIRTUAL_THREAD_EXECUTOR)
		.thenAcceptAsync(data -> setData(data, setter), minecraft);
	}

	private static <T> @Nullable T loadData(Minecraft minecraft, String fileName, Codec<T> codec) {
		try (BufferedReader reader = minecraft.getResourceManager().openAsReader(SkyblockerMod.id("constants/" + fileName))) {
			return codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Constant Data] Failed to load data for {}.", fileName, e);

			return null;
		}
	}

	private static <T> void setData(@Nullable T data, Consumer<T> setter) {
		if (data != null) {
			setter.accept(data);
		}
	}
}
