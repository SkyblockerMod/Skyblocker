package de.hysky.skyblocker.skyblock.item;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.constants.PetNumbers;
import io.github.moulberry.repo.data.Rarity;
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
		// Temporary adjustment because networth calc (short for calculator) returns 100 for max lv
		if (this.type().equals("ROSE_DRAGON") || this.type().equals("JADE_DRAGON")) {
			return LevelFinder.getLevelInfo("PET_GREG", (long) this.exp()).level;
		}
		var convertedPetInfo = new net.azureaaron.networth.item.PetInfo(this.type(), this.exp(), this.tier().name(), 0, Optional.empty(), Optional.empty());
		return PetCalculator.calculatePetLevel(convertedPetInfo).leftInt();
	}

	public int maxLevel() {
		if (NEURepoManager.isLoading()) return 100;
		if (rarity() == SkyblockItemRarity.UNKNOWN) return 100;

		var preRarity = NEURepoManager.getConstants().getPetNumbers().get(type());
		if (preRarity == null) return 100;
		PetNumbers neuPetData = preRarity.get(Rarity.valueOf(rarity().name()));
		if (neuPetData == null) return 100;

		int max = neuPetData.getMaxLevel();

		// Temporary adjustment because NEU repo doesn't have the max levels yet
		if (type().equals("ROSE_DRAGON") || type().equals("JADE_DRAGON")) {
			max = 200;
		}

		return max <= 0 ? 100 : max;
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}
}
