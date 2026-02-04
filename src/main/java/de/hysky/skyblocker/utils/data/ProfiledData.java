package de.hysky.skyblocker.utils.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.StringRepresentable;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/*
	This implementation doesn't have a default value parameter as there is no sensible default for the data structure.
	The data itself, however, is initialized to an empty map; so the data will never be null, but it can be empty.
*/
public class ProfiledData<T> extends JsonData<Object2ObjectOpenHashMap<UUID, Object2ObjectOpenHashMap<String, T>>> {
	/**
	 * @param file  The file to load/save the data from/to.
	 * @param codec The codec to use for serializing/deserializing the data.
	 */
	public ProfiledData(Path file, Codec<T> codec) {
		this(file, codec, false);
	}

	/**
	 * @param file       The file to load/save the data from/to.
	 * @param codec      The codec to use for serializing/deserializing the data.
	 * @param compressed Whether the {@link JsonOps#COMPRESSED} should be used.
	 *                   When compressed, {@link StringRepresentable#fromEnum(Supplier)} will use the ordinals instead of {@link StringRepresentable#getSerializedName()}.
	 *                   When compressed, codecs built with {@link RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 */
	public ProfiledData(Path file, Codec<T> codec, boolean compressed) {
		this(file, codec, compressed, true, true);
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
	 *                   When compressed, {@link StringRepresentable#fromEnum(Supplier)} will use the ordinals instead of {@link StringRepresentable#getSerializedName()}.
	 *                   When compressed, codecs built with {@link RecordCodecBuilder} will be serialized as a list instead of a map.
	 *                   {@link JsonOps#COMPRESSED} is required for maps with non-string keys.
	 * @param loadAsync  Whether the data should be loaded asynchronously. Default true.
	 * @param saveAsync  Whether the data should be saved asynchronously. Default false.
	 *                   Do not save async if saving is done with {@link ClientLifecycleEvents#CLIENT_STOPPING}.
	 */
	public ProfiledData(Path file, Codec<T> codec, boolean compressed, boolean loadAsync, boolean saveAsync) {
		super(file,
				// Mojang's internal Codec implementation uses ImmutableMaps so we'll just xmap those away and type safety while we're at it :')
				Codec.unboundedMap(UUIDUtil.AUTHLIB_CODEC,
						Codec.unboundedMap(Codec.STRING, codec).xmap(Object2ObjectOpenHashMap::new, Function.identity())
				).xmap(Object2ObjectOpenHashMap::new, Function.identity()),
				new Object2ObjectOpenHashMap<>(),
				compressed,
				loadAsync,
				saveAsync);
	}

	/**
	 * Checks if the current player's UUID and profile ID are present in the data.
	 *
	 * @return true if the data contains the current player's UUID and profile ID, false otherwise.
	 */
	public boolean containsKey() {
		return containsKey(Utils.getUuid(), Utils.getProfileId());
	}

	/**
	 * Checks if the given UUID and profile ID are present in the data.
	 *
	 * @param uuid      The UUID of the player.
	 * @param profileId The profile ID of the player.
	 * @return true if the data contains the given UUID and profile ID, false otherwise.
	 */
	public boolean containsKey(UUID uuid, String profileId) {
		return getPlayerData(uuid).containsKey(profileId);
	}

	/**
	 * Gets the value for the current player's UUID and profile ID.
	 *
	 * @return The value, or null if not found.
	 */
	public @Nullable T get() {
		return get(Utils.getUuid(), Utils.getProfileId());
	}

	/**
	 * Gets the value for the given UUID and profile ID.
	 *
	 * @param uuid      The UUID of the player.
	 * @param profileId The profile ID of the player.
	 * @return The value, or null if not found.
	 */
	public @Nullable T get(UUID uuid, String profileId) {
		return getPlayerData(uuid).get(profileId);
	}

	/**
	 * Puts the value for the current player's UUID and profile ID.
	 *
	 * @param value The value to put.
	 * @return The previous value, or null if not found.
	 */
	public @Nullable T put(T value) {
		return put(Utils.getUuid(), Utils.getProfileId(), value);
	}

	/**
	 * Puts the value for the given UUID and profile ID.
	 *
	 * @param uuid      The UUID of the player.
	 * @param profileId The profile ID of the player.
	 * @param value     The value to put.
	 * @return The previous value, or null if not found.
	 */
	public @Nullable T put(UUID uuid, String profileId, T value) {
		return getPlayerData(uuid).put(profileId, value);
	}

	/**
	 * Puts the value for the current player's UUID and profile ID if it is absent.
	 *
	 * @param value The value to put.
	 * @return The previous value, or null if not found.
	 */
	public @Nullable T putIfAbsent(T value) {
		return putIfAbsent(Utils.getUuid(), Utils.getProfileId(), value);
	}

	/**
	 * Puts the value for the given UUID and profile ID if it is absent.
	 *
	 * @param uuid      The UUID of the player.
	 * @param profileId The profile ID of the player.
	 * @param value     The value to put.
	 * @return The previous value, or null if not found.
	 */
	public @Nullable T putIfAbsent(UUID uuid, String profileId, T value) {
		return getPlayerData(uuid).putIfAbsent(profileId, value);
	}

	/**
	 * Computes the value for the current player's UUID and profile ID if it is absent.
	 *
	 * @param valueSupplier The supplier to compute the value.
	 * @return The computed value, or null if not found.
	 */
	public @Nullable T computeIfAbsent(Supplier<T> valueSupplier) {
		return computeIfAbsent(Utils.getUuid(), Utils.getProfileId(), valueSupplier);
	}

	/**
	 * Computes the value for the given UUID and profile ID if it is absent.
	 *
	 * @param uuid          The UUID of the player.
	 * @param profileId     The profile ID of the player.
	 * @param valueSupplier The supplier to compute the value.
	 * @return The computed value, or null if not found.
	 */
	public @Nullable T computeIfAbsent(UUID uuid, String profileId, Supplier<T> valueSupplier) {
		return getPlayerData(uuid).computeIfAbsent(profileId, _profileId -> valueSupplier.get());
	}

	/**
	 * Removes the value for the current player's UUID and profile ID.
	 *
	 * @return The removed value, or null if not found.
	 */
	public @Nullable T remove() {
		return remove(Utils.getUuid(), Utils.getProfileId());
	}

	/**
	 * Removes the value for the given UUID and profile ID.
	 *
	 * @param uuid      The UUID of the player.
	 * @param profileId The profile ID of the player.
	 * @return The removed value, or null if not found.
	 */
	public @Nullable T remove(UUID uuid, String profileId) {
		return getPlayerData(uuid).remove(profileId);
	}

	/**
	 * Gets the player data map for the given UUID.
	 *
	 * @param uuid The UUID of the player.
	 * @return The player data map.
	 */
	private Map<String, T> getPlayerData(UUID uuid) {
		return getData().computeIfAbsent(uuid, _uuid -> new Object2ObjectOpenHashMap<>());
	}
}
