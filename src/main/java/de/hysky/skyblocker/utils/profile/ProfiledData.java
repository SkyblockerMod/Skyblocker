package de.hysky.skyblocker.utils.profile;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProfiledData<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProfiledData.class);
	private final Path file;
	private final Codec<Object2ObjectOpenHashMap<UUID, Object2ObjectOpenHashMap<String, T>>> codec;
	private final boolean compressed;
	private final boolean loadAsync;
	private final boolean saveAsync;
	private Object2ObjectOpenHashMap<UUID, Object2ObjectOpenHashMap<String, T>> data = new Object2ObjectOpenHashMap<>();

	public ProfiledData(Path file, Codec<T> codec) {
		this(file, codec, false);
	}

	public ProfiledData(Path file, Codec<T> codec, boolean compressed) {
		this(file, codec, compressed, true, false);
	}

	public ProfiledData(Path file, Codec<T> codec, boolean loadAsync, boolean saveAsync) {
		this(file, codec, false, loadAsync, saveAsync);
	}

	/**
	 * @param compressed Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                   When compressed, {@link net.minecraft.util.StringIdentifiable#createCodec(Supplier)} will use the ordinals instead of {@link StringIdentifiable#asString()}.
	 *                   When compressed, codecs built with {@link com.mojang.serialization.codecs.RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 * @param loadAsync  Whether the data should be loaded asynchronously. Default true.
	 * @param saveAsync  Whether the data should be saved asynchronously. Default false.
	 *                   Do not save async if saving is done with {@link ClientLifecycleEvents#CLIENT_STOPPING}.
	 */
	public ProfiledData(Path file, Codec<T> codec, boolean compressed, boolean loadAsync, boolean saveAsync) {
		this.file = file;
		// Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
		this.codec = Codec.unboundedMap(Uuids.CODEC, Codec.unboundedMap(Codec.STRING, codec)
				.xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity());
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
			LOGGER.error("[Skyblocker Profiled Data] Failed to load data from file: {}", file, e);
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
			LOGGER.error("[Skyblocker Profiled Data] Failed to create directories for file: {}", file, e);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			SkyblockerMod.GSON.toJson(codec.encodeStart(compressed ? JsonOps.COMPRESSED : JsonOps.INSTANCE, data).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profiled Data] Failed to save data to file: {}", file, e);
		}
	}

	public boolean containsKey() {
		return containsKey(Utils.getUuid(), Utils.getProfileId());
	}

	public boolean containsKey(UUID uuid, String profileId) {
		return getPlayerData(uuid).containsKey(profileId);
	}

	@Nullable
	public T get() {
		return get(Utils.getUuid(), Utils.getProfileId());
	}

	@Nullable
	public T get(UUID uuid, String profileId) {
		return getPlayerData(uuid).get(profileId);
	}

	public T put(T value) {
		return put(Utils.getUuid(), Utils.getProfileId(), value);
	}

	public T put(UUID uuid, String profileId, T value) {
		return getPlayerData(uuid).put(profileId, value);
	}

	public T putIfAbsent(T value) {
		return putIfAbsent(Utils.getUuid(), Utils.getProfileId(), value);
	}

	public T putIfAbsent(UUID uuid, String profileId, T value) {
		return getPlayerData(uuid).putIfAbsent(profileId, value);
	}

	public T computeIfAbsent(Supplier<T> valueSupplier) {
		return computeIfAbsent(Utils.getUuid(), Utils.getProfileId(), valueSupplier);
	}

	public T computeIfAbsent(UUID uuid, String profileId, Supplier<T> valueSupplier) {
		return getPlayerData(uuid).computeIfAbsent(profileId, _profileId -> valueSupplier.get());
	}

	public T remove() {
		return remove(Utils.getUuid(), Utils.getProfileId());
	}

	public T remove(UUID uuid, String profileId) {
		return getPlayerData(uuid).remove(profileId);
	}

	private Map<String, T> getPlayerData(UUID uuid) {
		return data.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
	}
}
