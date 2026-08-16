package de.hysky.skyblocker.skyblock.hunting;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AttributesFile(List<Attribute> attributes) {
	public static final Codec<AttributesFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Attribute.LIST_CODEC.fieldOf("attributes").forGetter(AttributesFile::attributes)
	).apply(instance, AttributesFile::new));
}
