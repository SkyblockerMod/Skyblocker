package de.hysky.skyblocker.skyblock.item;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PetInfo(String type, double exp, String tier, Optional<String> uuid, Optional<String> item, Optional<String> skin) {
	public static final Codec<PetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(PetInfo::type),
			Codec.DOUBLE.fieldOf("exp").forGetter(PetInfo::exp),
			Codec.STRING.fieldOf("tier").forGetter(PetInfo::tier),
			Codec.STRING.optionalFieldOf("uuid").forGetter(PetInfo::uuid),
			Codec.STRING.optionalFieldOf("heldItem").forGetter(PetInfo::item),
			Codec.STRING.optionalFieldOf("skin").forGetter(PetInfo::skin)
	).apply(instance, PetInfo::new));
	public static final PetInfo EMPTY = new PetInfo("", 0, "", Optional.empty(), Optional.empty(), Optional.empty());

	public SkyblockItemRarity rarity() {
		return SkyblockItemRarity.valueOf(tier);
	}

	public int tierIndex() {
		return rarity().ordinal();
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
