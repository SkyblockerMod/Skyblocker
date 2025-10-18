package de.hysky.skyblocker.injected;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import org.jetbrains.annotations.NotNull;

import de.hysky.skyblocker.skyblock.item.PetInfo;

public interface SkyblockerStack {
	@NotNull
	default String getSkyblockId() {
		return "";
	}

	@NotNull
	default String getSkyblockApiId() {
		return "";
	}

	@NotNull
	default String getNeuName() {
		return "";
	}

	@NotNull
	default String getUuid() {
		return "";
	}

	@NotNull
	default PetInfo getPetInfo() {
		return PetInfo.EMPTY;
	}

	@NotNull
	default SkyblockItemRarity getSkyblockRarity() { return SkyblockItemRarity.UNKNOWN; }
}
