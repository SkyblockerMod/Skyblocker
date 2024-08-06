package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.NotNull;

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
}
