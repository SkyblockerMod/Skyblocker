package de.hysky.skyblocker.utils.profile;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ProfiledData<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProfiledData.class);
	private final Path file;
	private final Codec<Map<String, Map<String, T>>> codec;
	private Map<String, Map<String, T>> data = new HashMap<>();

	public ProfiledData(Path file, Codec<T> codec) {
		this.file = file;
		this.codec = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, codec));
	}

	public void init() {
		load();
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
	}

	// Note: JsonOps.COMPRESSED must be used if you're using maps with non-string keys
	public CompletableFuture<Void> load() {
		return CompletableFuture.runAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(file)) {
				// Atomic operation to prevent concurrent modification
				data = codec.parse(JsonOps.COMPRESSED, SkyblockerMod.GSON.fromJson(reader, JsonObject.class)).getOrThrow();
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Profiled Data] Failed to load data from file: {}", file, e);
			}
		});
	}

	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			SkyblockerMod.GSON.toJson(codec.encodeStart(JsonOps.COMPRESSED, data).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profiled Data] Failed to save data to file: {}", file, e);
		}
	}

	public boolean containsKey() {
		return containsKey(Utils.getUndashedUuid(), Utils.getProfileId());
	}

	public boolean containsKey(String uuid, String profileId) {
		return getPlayerData(uuid).containsKey(profileId);
	}

	public T get() {
		return get(Utils.getUndashedUuid(), Utils.getProfileId());
	}

	public T get(String uuid, String profileId) {
		return getPlayerData(uuid).get(profileId);
	}

	public T put(T value) {
		return put(Utils.getUndashedUuid(), Utils.getProfileId(), value);
	}

	public T put(String uuid, String profileId, T value) {
		return getPlayerData(uuid).put(profileId, value);
	}

	public T putIfAbsent(T value) {
		return putIfAbsent(Utils.getUndashedUuid(), Utils.getProfileId(), value);
	}

	public T putIfAbsent(String uuid, String profileId, T value) {
		return getPlayerData(uuid).putIfAbsent(profileId, value);
	}

	public T computeIfAbsent(Supplier<T> valueSupplier) {
		return computeIfAbsent(Utils.getUndashedUuid(), Utils.getProfileId(), valueSupplier);
	}

	public T computeIfAbsent(String uuid, String profileId, Supplier<T> valueSupplier) {
		return getPlayerData(uuid).computeIfAbsent(profileId, _profileId -> valueSupplier.get());
	}

	public T remove() {
		return remove(Utils.getUndashedUuid(), Utils.getProfileId());
	}

	public T remove(String uuid, String profileId) {
		return getPlayerData(uuid).remove(profileId);
	}

	private Map<String, T> getPlayerData(String uuid) {
		return data.computeIfAbsent(uuid, _uuid -> new HashMap<>());
	}
}
