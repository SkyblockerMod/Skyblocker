package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.NotNull;

import de.hysky.skyblocker.skyblock.item.PetInfo;

public interface SkyblockerStack {
	@NotNull
	default String skyblocker$getSkyblockId() {
		return "";
	}

	@NotNull
	default String skyblocker$getSkyblockApiId() {
		return "";
	}

	@NotNull
	default String skyblocker$getNeuName() {
		return "";
	}

	@NotNull
	default String skyblocker$getUuid() {
		return "";
	}

	@NotNull
	default PetInfo skyblocker$getPetInfo() {
		return PetInfo.EMPTY;
	}
}
