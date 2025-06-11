package de.hysky.skyblocker.utils.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProfiledData<T> extends JsonData<Object2ObjectOpenHashMap<UUID, Object2ObjectOpenHashMap<String, T>>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProfiledData.class);

	/**
	 * @param file  The file to load/save the data from/to.
	 * @param codec The codec to use for serializing/deserializing the data.
	 */
	public ProfiledData(Path file, Codec<T> codec) {
		this(file, codec, false);
	}

	/**
	 * @param file      The file to load/save the data from/to.
	 * @param codec     The codec to use for serializing/deserializing the data.
	 * @param compressed Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                   When compressed, {@link net.minecraft.util.StringIdentifiable#createCodec(Supplier)} will use the ordinals instead of {@link StringIdentifiable#asString()}.
	 *                   When compressed, codecs built with {@link com.mojang.serialization.codecs.RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 */
	public ProfiledData(Path file, Codec<T> codec, boolean compressed) {
		this(file, codec, compressed, true, false);
	}

	/**
	 * @param file      The file to load/save the data from/to.
	 * @param codec     The codec to use for serializing/deserializing the data.
	 * @param loadAsync Whether the data should be loaded asynchronously. Default true.
	 * @param saveAsync Whether the data should be saved asynchronously. Default false.
	 *                  Do not save async if saving is done with {@link ClientLifecycleEvents#CLIENT_STOPPING}.
	 */
	public ProfiledData(Path file, Codec<T> codec, boolean loadAsync, boolean saveAsync) {
		this(file, codec, false, loadAsync, saveAsync);
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
	public ProfiledData(Path file, Codec<T> codec, boolean compressed, boolean loadAsync, boolean saveAsync) {
		super(file,
				// Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
				Codec.unboundedMap(Uuids.CODEC, Codec.unboundedMap(Codec.STRING, codec)
				.xmap(Object2ObjectOpenHashMap::new, Function.identity())
		).xmap(Object2ObjectOpenHashMap::new, Function.identity()),
				compressed,
				loadAsync,
				saveAsync);
	}

	public boolean containsKey() {
		return containsKey(Utils.getUuid(), Utils.getProfileId());
	}

	public boolean containsKey(@NotNull UUID uuid, @NotNull String profileId) {
		return getPlayerData(uuid).containsKey(profileId);
	}

	@Nullable
	public T get() {
		return get(Utils.getUuid(), Utils.getProfileId());
	}

	@Nullable
	public T get(@NotNull UUID uuid, @NotNull String profileId) {
		return getPlayerData(uuid).get(profileId);
	}

	@Nullable
	public T put(@NotNull T value) {
		return put(Utils.getUuid(), Utils.getProfileId(), value);
	}

	@Nullable
	public T put(@NotNull UUID uuid, @NotNull String profileId, @NotNull T value) {
		return getPlayerData(uuid).put(profileId, value);
	}

	@Nullable
	public T putIfAbsent(@NotNull T value) {
		return putIfAbsent(Utils.getUuid(), Utils.getProfileId(), value);
	}

	@Nullable
	public T putIfAbsent(@NotNull UUID uuid, @NotNull String profileId, @NotNull T value) {
		return getPlayerData(uuid).putIfAbsent(profileId, value);
	}

	@Nullable
	public T computeIfAbsent(@NotNull Supplier<T> valueSupplier) {
		return computeIfAbsent(Utils.getUuid(), Utils.getProfileId(), valueSupplier);
	}

	@Nullable
	public T computeIfAbsent(@NotNull UUID uuid, @NotNull String profileId, @NotNull Supplier<T> valueSupplier) {
		return getPlayerData(uuid).computeIfAbsent(profileId, _profileId -> valueSupplier.get());
	}

	@Nullable
	public T remove() {
		return remove(Utils.getUuid(), Utils.getProfileId());
	}

	@Nullable
	public T remove(@NotNull UUID uuid, @NotNull String profileId) {
		return getPlayerData(uuid).remove(profileId);
	}

	private Map<String, T> getPlayerData(UUID uuid) {
		if (data == null) {
			LOGGER.error("[Skyblocker Profiled Data] Tried to get player data while data is null: `{}`", uuid);
			return new Object2ObjectOpenHashMap<>();
		}
		return data.computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
	}
}
