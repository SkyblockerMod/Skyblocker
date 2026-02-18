package de.hysky.skyblocker.utils.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.backup.ConfigBackupManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class JsonData<T> {
	public static final SystemToast.SystemToastId ERROR_TOAST_ID = new SystemToast.SystemToastId(10_000L);
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonData.class);
	private final Path file;
	private final Codec<T> codec;
	private final boolean compressed; // Default: false
	private final boolean loadAsync;  // Default: true
	private final boolean saveAsync;  // Default: false
	private T data; // Default: defaultValue
	private @Nullable CompletableFuture<Void> loaded;

	/**
	 * @param file         The file to load/save the data from/to.
	 * @param codec        The codec to use for serializing/deserializing the data.
	 * @param defaultValue The default value of {@link #data} to use in case the file does not exist yet.
	 */
	public JsonData(Path file, Codec<T> codec, T defaultValue) {
		this(file, codec, defaultValue, false);
	}

	/**
	 * @param file         The file to load/save the data from/to.
	 * @param codec        The codec to use for serializing/deserializing the data.
	 * @param defaultValue The default value of {@link #data} to use in case the file does not exist yet.
	 * @param loadAsync    Whether the data should be loaded asynchronously.
	 * @param saveAsync    Whether the data should be saved asynchronously.
	 */
	public JsonData(Path file, Codec<T> codec, T defaultValue, boolean loadAsync, boolean saveAsync) {
		this(file, codec, defaultValue, false, loadAsync, saveAsync);
	}

	/**
	 * @param file         The file to load/save the data from/to.
	 * @param codec        The codec to use for serializing/deserializing the data.
	 * @param defaultValue The default value of {@link #data} to use in case the file does not exist yet.
	 * @param compressed   Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                     When compressed, {@link StringRepresentable#fromEnum(Supplier)} will use the ordinals instead of {@link StringRepresentable#getSerializedName()}.
	 *                     When compressed, codecs built with {@link RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                     {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 */
	public JsonData(Path file, Codec<T> codec, T defaultValue, boolean compressed) {
		this(file, codec, defaultValue, compressed, true, true);
	}

	/**
	 * @param file         The file to load/save the data from/to.
	 * @param codec        The codec to use for serializing/deserializing the data.
	 * @param defaultValue The default value of {@link #data} to use in case the file does not exist c.
	 * @param compressed   Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                     When compressed, {@link StringRepresentable#fromEnum(Supplier)} will use the ordinals instead of {@link StringRepresentable#getSerializedName()}.
	 *                     When compressed, codecs built with {@link RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                     {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 * @param loadAsync    Whether the data should be loaded asynchronously.
	 * @param saveAsync    Whether the data should be saved asynchronously.
	 *                     Do not save async if saving is done with {@link ClientLifecycleEvents#CLIENT_STOPPING}.
	 */
	public JsonData(Path file, Codec<T> codec, T defaultValue, boolean compressed, boolean loadAsync, boolean saveAsync) {
		this.file = file;
		this.codec = codec;
		this.data = defaultValue;
		this.compressed = compressed;
		this.loadAsync = loadAsync;
		this.saveAsync = saveAsync;
	}

	/**
	 * Initializes the data by registering a save listener for when the client stops.
	 * This will also load the data from the file.
	 *
	 * @return A CompletableFuture that completes when the data has loaded.
	 * @implNote The CompletableFuture does not provide the data. Use {@link #getData()}.
	 * There's no need to set the data manually with the result of the completed future, as that is already done in {@link #loadInternal()}.
	 */
	public CompletableFuture<Void> init() {
		// Make sure saving always completes by waiting on the save CompletableFuture.
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save().join());
		return load();
	}

	public CompletableFuture<Void> load() {
		if (loadAsync) {
			loaded = CompletableFuture.runAsync(this::loadInternal, Executors.newVirtualThreadPerTaskExecutor());
		} else {
			loadInternal();
			loaded = CompletableFuture.completedFuture(null);
		}
		return loaded;
	}

	// Note: JsonOps.COMPRESSED must be used if you're using maps with non-string keys
	private void loadInternal() {
		boolean createBackup = false;
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			DataResult<T> parsed = codec.parse(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, JsonParser.parseReader(reader));
			parsed.resultOrPartial(s -> LOGGER.error("[Skyblocker Json Data] Failed to parse data from file: `{}`. {}", file, s))
					.ifPresent(t -> data = t); // Atomic operation to prevent concurrent modification
			createBackup = parsed.isError();
		} catch (NoSuchFileException ignored) {
		} catch (Exception e) {
			createBackup = true;
			LOGGER.error("[Skyblocker Json Data] Failed to load data from file: `{}`", file, e);
		}
		if (createBackup) {
			Minecraft.getInstance().getToastManager().addToast(SystemToast.multiline(
					Minecraft.getInstance(), ERROR_TOAST_ID,
					Component.literal("Skyblocker Config Error"),
					Component.literal("Failed to load '" + FabricLoader.getInstance().getConfigDir().relativize(file) + "'")
							.append("\n")
							.append("See logs for details. A backup of the file has been made.")
			));
			try {
				// future-proof in case we use other things apart from json
				String fileName = file.getFileName().toString();
				int extensionIndex = fileName.lastIndexOf('.');
				String newFileName;
				if (extensionIndex >= 0) {
					String extension = fileName.substring(extensionIndex);
					newFileName = fileName.substring(0, extensionIndex) + '_' + ConfigBackupManager.FORMATTER.format(LocalDateTime.now()) + extension;
				} else {
					LOGGER.warn("[Skyblocker Json Data] No extension? {}", file);
					newFileName = fileName + '_' + ConfigBackupManager.FORMATTER.format(LocalDateTime.now());
				}
				Files.copy(file, file.getParent().resolve(newFileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOGGER.error("[Skyblocker Json Data] Failed to create backup for file: `{}`", file, e);
			}
		}
	}

	public CompletableFuture<Void> save() {
		if (saveAsync && Minecraft.getInstance().isRunning()) { // Do not save async if we are closing the game
			return CompletableFuture.runAsync(this::saveInternal, Executors.newVirtualThreadPerTaskExecutor());
		} else {
			saveInternal();
			return CompletableFuture.completedFuture(null);
		}
	}

	private void saveInternal() {
		if (loaded == null) {
			LOGGER.error("[Skyblocker Json Data] Save data called when loading has not started for file `{}`. This will override the contents of the file with the default value.", file);
		} else if (!isLoaded()) {
			LOGGER.warn("[Skyblocker Json Data] Save data called when loading has not finished for file `{}`. Blocking until data is loaded.", file);
			loaded.join();
		}

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

	/**
	 * @deprecated Use {@link #isLoaded()} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean isEmpty() {
		return !isLoaded();
	}

	public boolean isLoaded() {
		return loaded != null && loaded.isDone();
	}

	public T getData() {
		if (loaded == null) {
			LOGGER.error("[Skyblocker Json Data] Get data called when loading has not started for file `{}`. Returning default value.", file);
		} else if (!isLoaded()) {
			LOGGER.warn("[Skyblocker Json Data] Get data called when loading has not finished for file `{}`. Blocking until data is loaded.", file);
			loaded.join();
		}
		return data;
	}

	/**
	 * Sets the data to the given value and returns the old value.
	 *
	 * @param data The new data to set.
	 * @return The old data before setting the new one.
	 */
	public T setData(T data) {
		T oldData = this.data;
		this.data = data;
		return oldData;
	}
}
