package de.hysky.skyblocker.injected;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface SkyblockerEntity {
	default @Nullable Component skyblocker$getCustomName() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}

	default void skyblocker$setCustomName(Component customName) {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
