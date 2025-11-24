package de.hysky.skyblocker.skyblock.item;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.azureaaron.networth.PetCalculator;

public record PetInfo(Optional<String> name, String type, double exp, SkyblockItemRarity tier, Optional<String> uuid, Optional<String> item, Optional<String> skin) {
	public static final Codec<PetInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("name").forGetter(PetInfo::name),
			Codec.STRING.fieldOf("type").forGetter(PetInfo::type),
			Codec.DOUBLE.optionalFieldOf("exp", 0d).forGetter(PetInfo::exp),
			SkyblockItemRarity.CODEC.fieldOf("tier").forGetter(PetInfo::tier),
			Codec.STRING.optionalFieldOf("uuid").forGetter(PetInfo::uuid),
			Codec.STRING.optionalFieldOf("heldItem").forGetter(PetInfo::item),
			Codec.STRING.optionalFieldOf("skin").forGetter(PetInfo::skin)
	).apply(instance, PetInfo::new));
	public static final PetInfo EMPTY = new PetInfo(Optional.empty(), "", 0, SkyblockItemRarity.UNKNOWN, Optional.empty(), Optional.empty(), Optional.empty());

	public SkyblockItemRarity rarity() {
		return this.tier;
	}

	public int tierIndex() {
		return rarity().ordinal();
	}

	public int level() {
		var convertedPetInfo = new net.azureaaron.networth.item.PetInfo(this.type(), this.exp(), this.tier().name(), 0, Optional.empty(), Optional.empty());
		return PetCalculator.calculatePetLevel(convertedPetInfo).leftInt();
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}
}
