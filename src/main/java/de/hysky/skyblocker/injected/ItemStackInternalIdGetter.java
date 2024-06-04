package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.Nullable;

public interface ItemStackInternalIdGetter {
	@Nullable
	default String getInternalId() {
		return "";
	}

	@Nullable
	default String getInternalName() {
		return "";
	}

	@Nullable
	default String getNeuName() {
		return "";
	}
}
