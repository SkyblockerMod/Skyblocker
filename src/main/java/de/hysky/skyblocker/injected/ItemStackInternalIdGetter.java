package de.hysky.skyblocker.injected;

public interface ItemStackInternalIdGetter {
	default String skyblocker$getInternalId(boolean internalIdOnly) {
		return "";
	}

	default String skyblocker$getNeuName() {
		return "";
	}
}
