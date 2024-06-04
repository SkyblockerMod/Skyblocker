package de.hysky.skyblocker.injected;

public interface ItemStackInternalIdGetter {
	default String getInternalId() {
		return "";
	}

	default String getInternalName() {
		return "";
	}

	default String getNeuName() {
		return "";
	}
}
