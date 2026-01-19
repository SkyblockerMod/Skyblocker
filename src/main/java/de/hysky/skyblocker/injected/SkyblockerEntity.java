package de.hysky.skyblocker.injected;

import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public interface SkyblockerEntity {
	default @Nullable Component skyblocker$getCustomName() {
		return null;
	}

	default void skyblocker$setCustomName(Component customName) {
	}
}
