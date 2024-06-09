package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.Nullable;

public interface SkyblockerStack {
	@Nullable
	default String getSkyblockId() {
		return "";
	}

	@Nullable
	default String getSkyblockApiId() {
		return "";
	}

	@Nullable
	default String getNeuName() {
		return "";
	}
}
