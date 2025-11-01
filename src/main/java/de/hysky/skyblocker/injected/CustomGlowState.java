package de.hysky.skyblocker.injected;

public interface CustomGlowState {

	default void markCustomGlow() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}

	default boolean hasCustomGlow() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
