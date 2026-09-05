package de.hysky.skyblocker.utils.mayor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CandidatePerk(String name, String description, boolean minister) {
	public static final Codec<CandidatePerk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(CandidatePerk::name),
			Codec.STRING.fieldOf("description").forGetter(CandidatePerk::description),
			Codec.BOOL.fieldOf("minister").forGetter(CandidatePerk::minister)
	).apply(instance, CandidatePerk::new));
}
