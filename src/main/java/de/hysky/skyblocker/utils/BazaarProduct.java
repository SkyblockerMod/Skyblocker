package de.hysky.skyblocker.utils;

import java.util.OptionalDouble;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public record BazaarProduct(String id, OptionalDouble buyPrice, OptionalDouble sellPrice, int buyVolume, int sellVolume) {
	private static final Codec<BazaarProduct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("id").forGetter(BazaarProduct::id),
			CodecUtils.optionalDouble(Codec.DOUBLE.lenientOptionalFieldOf("buyPrice")).forGetter(BazaarProduct::buyPrice),
			CodecUtils.optionalDouble(Codec.DOUBLE.lenientOptionalFieldOf("sellPrice")).forGetter(BazaarProduct::sellPrice),
			Codec.INT.fieldOf("buyVolume").forGetter(BazaarProduct::buyVolume),
			Codec.INT.fieldOf("sellVolume").forGetter(BazaarProduct::sellVolume))
			.apply(instance, BazaarProduct::new));
	public static final Codec<Object2ObjectMap<String, BazaarProduct>> MAP_CODEC = CodecUtils.object2ObjectMapCodec(Codec.STRING, CODEC);
}
