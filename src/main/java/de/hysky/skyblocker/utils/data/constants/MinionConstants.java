package de.hysky.skyblocker.utils.data.constants;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MinionConstants(Map<String, List<String>> categories) {
	public static final Codec<MinionConstants> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).fieldOf("categories").forGetter(MinionConstants::categories)
			).apply(instance, MinionConstants::new));
	public static final MinionConstants EMPTY = new MinionConstants(Map.of());
}
