package de.hysky.skyblocker.skyblock.hunting;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * @param name Ability name
 * @param shardName Shard item name
 * @param id Alphanumeric ID - found in the Hunting Box
 * @param apiId Skyblock API Id
 * @param neuId NEU Item Id
 */
public record Attribute(String name, String shardName, String id, String apiId, String neuId) {
	private static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("abilityName").forGetter(Attribute::name),
			Codec.STRING.fieldOf("displayName").forGetter(Attribute::shardName),
			Codec.STRING.fieldOf("shardId").forGetter(Attribute::id),
			Codec.STRING.fieldOf("bazaarName").forGetter(Attribute::apiId),
			Codec.STRING.fieldOf("internalName").forGetter(Attribute::neuId)
			).apply(instance, Attribute::new));
	public static final Codec<List<Attribute>> LIST_CODEC = CODEC.listOf();
}
