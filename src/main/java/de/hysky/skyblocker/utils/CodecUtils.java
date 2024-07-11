package de.hysky.skyblocker.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CodecUtils {

	public static <K> Codec<Object2IntMap<K>> createObject2IntMapCodec(Codec<K> keyCodec) {
		return Codec.unboundedMap(keyCodec, Codec.INT)
				.flatComapMap(Object2IntOpenHashMap::new, map -> DataResult.success(new Object2IntOpenHashMap<>(map)));
	}

	public static <K, V> Codec<Object2ObjectMap<K, V>> createObject2ObjectMapCodec(Codec<K> keyCodec, Codec<V> valueCodec) {
		return Codec.unboundedMap(keyCodec, valueCodec)
				.flatComapMap(Object2ObjectOpenHashMap::new, map -> DataResult.success(new Object2ObjectOpenHashMap<>(map)));
	}
}
