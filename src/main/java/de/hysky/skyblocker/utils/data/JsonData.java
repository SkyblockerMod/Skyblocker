package de.hysky.skyblocker.utils.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class JsonData<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonData.class);
	protected final Path file;
	protected final Codec<T> codec;
	protected final boolean compressed;
	protected final boolean loadAsync;
	protected final boolean saveAsync;
	@Nullable
	protected T data;

	/**
	 * @param file  The file to load/save the data from/to.
	 * @param codec The codec to use for serializing/deserializing the data.
	 */
	public JsonData(Path file, Codec<T> codec) {
		this(file, codec, false);
	}

	/**
	 * @param file      The file to load/save the data from/to.
	 * @param codec     The codec to use for serializing/deserializing the data.
	 * @param loadAsync Whether the data should be loaded asynchronously. Default true.
	 * @param saveAsync Whether the data should be saved asynchronously. Default false.
	 */
	public JsonData(Path file, Codec<T> codec, boolean loadAsync, boolean saveAsync) {
		this(file, codec, false, loadAsync, saveAsync);
	}

	/**
	 * @param file       The file to load/save the data from/to.
	 * @param codec      The codec to use for serializing/deserializing the data.
	 * @param compressed Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                   When compressed, {@link net.minecraft.util.StringIdentifiable#createCodec(Supplier)} will use the ordinals instead of {@link StringIdentifiable#asString()}.
	 *                   When compressed, codecs built with {@link com.mojang.serialization.codecs.RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 */
	public JsonData(Path file, Codec<T> codec, boolean compressed) {
		this(file, codec, compressed, true, false);
	}

	/**
	 * @param file       The file to load/save the data from/to.
	 * @param codec      The codec to use for serializing/deserializing the data.
	 * @param compressed Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                   When compressed, {@link net.minecraft.util.StringIdentifiable#createCodec(Supplier)} will use the ordinals instead of {@link StringIdentifiable#asString()}.
	 *                   When compressed, codecs built with {@link com.mojang.serialization.codecs.RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 * @param loadAsync  Whether the data should be loaded asynchronously. Default true.
	 * @param saveAsync  Whether the data should be saved asynchronously. Default false.
	 *                   Do not save async if saving is done with {@link ClientLifecycleEvents#CLIENT_STOPPING}.
	 */
	public JsonData(Path file, Codec<T> codec, boolean compressed, boolean loadAsync, boolean saveAsync) {
		this.file = file;
		this.codec = codec;
		this.compressed = compressed;
		this.loadAsync = loadAsync;
		this.saveAsync = saveAsync;
	}

	public CompletableFuture<Void> init() {
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
		return load();
	}

	public CompletableFuture<Void> load() {
		if (loadAsync) {
			return CompletableFuture.runAsync(this::loadInternal);
		} else {
			loadInternal();
			return CompletableFuture.completedFuture(null);
		}
	}

	// Note: JsonOps.COMPRESSED must be used if you're using maps with non-string keys
	private void loadInternal() {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			// Atomic operation to prevent concurrent modification
			data = codec.parse(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, SkyblockerMod.GSON.fromJson(reader, JsonObject.class)).getOrThrow();
		} catch (NoSuchFileException ignored) {
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Json Data] Failed to load data from file: `{}`", file, e);
		}
	}

	public CompletableFuture<Void> save() {
		if (saveAsync) {
			return CompletableFuture.runAsync(this::saveInternal);
		} else {
			saveInternal();
			return CompletableFuture.completedFuture(null);
		}
	}

	private void saveInternal() {
		try {
			Files.createDirectories(file.getParent());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Json Data] Failed to create directories for file: `{}`", file, e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			SkyblockerMod.GSON.toJson(codec.encodeStart(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, data).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Json Data] Failed to save data to file: `{}`", file, e);
		}
	}

	@Nullable
	public T getData() {
		return data;
	}
}
