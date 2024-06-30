package de.hysky.skyblocker.injected;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.stp.SkyblockerPackMetadata;

public interface SkyblockerResourcePackProfile {

	default void setSkyblockerMetadata(SkyblockerPackMetadata metadata) {
	}

	@Nullable
	default SkyblockerPackMetadata getSkyblockerMetadata() {
		return null;
	}
}
