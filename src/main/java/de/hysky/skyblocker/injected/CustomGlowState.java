package de.hysky.skyblocker.injected;

public interface CustomGlowState {

	default void skyblocker$markCustomGlow() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}

	default boolean skyblocker$hasCustomGlow() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
