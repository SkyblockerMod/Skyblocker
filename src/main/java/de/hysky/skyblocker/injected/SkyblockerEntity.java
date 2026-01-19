package de.hysky.skyblocker.injected;

import net.minecraft.network.chat.Component;

import org.jspecify.annotations.Nullable;

public interface SkyblockerEntity {
	default @Nullable Component skyblocker$getCustomName() {
		return null;
	}

	default void skyblocker$setCustomName(Component customName) {
	}
}
