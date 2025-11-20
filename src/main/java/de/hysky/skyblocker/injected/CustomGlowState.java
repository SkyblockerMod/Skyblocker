package de.hysky.skyblocker.injected;

public interface CustomGlowState {

	default void skyblocker$setCustomGlowColour(int glowColour) {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}

	default int skyblocker$getCustomGlowColour() {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
