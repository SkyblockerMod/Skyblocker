package de.hysky.skyblocker.skyblock.hunting;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Attribute(String name, String shardName, String id, String apiId) {
	private static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(Attribute::name),
			Codec.STRING.fieldOf("shardName").forGetter(Attribute::shardName),
			Codec.STRING.fieldOf("id").forGetter(Attribute::id),
			Codec.STRING.fieldOf("apiId").forGetter(Attribute::apiId)
			).apply(instance, Attribute::new));
	public static final Codec<List<Attribute>> LIST_CODEC = CODEC.listOf();
}
