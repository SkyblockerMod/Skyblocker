package de.hysky.skyblocker.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public final class CodecUtils {
	public static final Codec<Color> COLOR_CODEC = Codec.INT.xmap(argb -> new Color(argb, true), Color::getRGB);

	private CodecUtils() {
		throw new IllegalStateException("Uhhhh no? like just no. What are you trying to do? D- Do you think this will be useful to instantiate this? Like it's private, so you went through the effort of putting an accessor actually i'm not sure you can accessor a constructor. can you? so if not did you really put an access widener for that? like really? honestly this is just sad. Plus there aren't even any method in here that requires an instance. There's only static methods. like bruh. you know what i'm done typing shit for you to read, bye i'm leaving *voice lowers as I leave* I swear those modders think they can access all they want sheesh *comes back instantly* AND I SWEAR IF YOU INJECT SO THIS ERROR CANNOT BE THROWN I WILL SEND YOU TO HELL'S FREEZER");
	}

	public static MapCodec<OptionalInt> optionalInt(MapCodec<Optional<Integer>> codec) {
		return codec.xmap(opt -> opt.map(OptionalInt::of).orElseGet(OptionalInt::empty), optInt -> optInt.isPresent() ? Optional.of(optInt.getAsInt()) : Optional.empty());
	}

	public static MapCodec<OptionalDouble> optionalDouble(MapCodec<Optional<Double>> codec) {
		return codec.xmap(opt -> opt.map(OptionalDouble::of).orElseGet(OptionalDouble::empty), optDouble -> optDouble.isPresent() ? Optional.of(optDouble.getAsDouble()) : Optional.empty());
	}

	/**
	 * @see #mutableOptional(MapCodec, Function) mutableOptional(MapCodec, Function) for important notes when using this codec for an optional field with a default value.
	 */
	public static <K> Codec<Object2BooleanMap<K>> object2BooleanMapCodec(Codec<K> keyCodec) {
		return Codec.unboundedMap(keyCodec, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Function.identity());
	}

	/**
	 * @see #mutableOptional(MapCodec, Function) mutableOptional(MapCodec, Function) for important notes when using this codec for an optional field with a default value.
	 */
	public static <K> Codec<Object2IntMap<K>> object2IntMapCodec(Codec<K> keyCodec) {
		return Codec.unboundedMap(keyCodec, Codec.INT).xmap(Object2IntOpenHashMap::new, Function.identity());
	}

	/**
	 * @see #mutableOptional(MapCodec, Function) mutableOptional(MapCodec, Function) for important notes when using this codec for an optional field with a default value.
	 */
	public static <K> Codec<Object2LongMap<K>> object2LongMapCodec(Codec<K> keyCodec) {
		return Codec.unboundedMap(keyCodec, Codec.LONG).xmap(Object2LongOpenHashMap::new, Function.identity());
	}

	/**
	 * @see #mutableOptional(MapCodec, Function) mutableOptional(MapCodec, Function) for important notes when using this codec for an optional field with a default value.
	 */
	public static <K> Codec<Object2DoubleMap<K>> object2DoubleMapCodec(Codec<K> keyCodec) {
		return Codec.unboundedMap(keyCodec, Codec.DOUBLE).xmap(Object2DoubleOpenHashMap::new, Function.identity());
	}

	/**
	 * @see #mutableOptional(MapCodec, Function) mutableOptional(MapCodec, Function) for important notes when using this codec for an optional field with a default value.
	 */
	public static <K, V> Codec<Object2ObjectMap<K, V>> object2ObjectMapCodec(Codec<K> keyCodec, Codec<V> valueCodec) {
		return Codec.unboundedMap(keyCodec, valueCodec).xmap(Object2ObjectOpenHashMap::new, Function.identity());
	}

	/**
	 * Creates a {@link EnumSet} codec for the given enum codec and class.
	 *
	 * @param enumCodec Codec of the enum
	 * @param <E>       The enum type
	 * @return EnumSet codec for the given enum
	 */
	public static <E extends Enum<E>> Codec<EnumSet<E>> enumSetCodec(Codec<E> enumCodec, Class<E> enumClass) {
		// EnumSet#copyOf finds type from the first element of the list passed to it, so if it's empty the enum type is unknown and an exception is thrown
		// So we have to manually handle the case where the list empty
		return enumCodec.listOf().xmap(list -> list.isEmpty() ? EnumSet.noneOf(enumClass) : EnumSet.copyOf(list), List::copyOf);
	}

	/**
	 * Maps a {@link MapCodec} to a mutable version using the given mutable factory, for use with optional fields to avoid mutating the default value.
	 *
	 * <p>For all mutable map codecs above, if the codec is used for an optional field with a default value,
	 * it is required to wrap the codec with this method or copy the map manually after deserialization.
	 * Otherwise, the default value can be mutated, leading to unexpected behavior including failure to serialize.
	 */
	public static <T> MapCodec<T> mutableOptional(MapCodec<T> codec, Function<? super T, ? extends T> mutableFactory) {
		return codec.xmap(mutableFactory, Function.identity());
	}
}
