package de.hysky.skyblocker.injected;

public interface RecipeBookHolder {

	/**
	 * Register a callback that gets called when the recipe book button is toggled.
	 * The callback list is emptied after each init, so this needs to be registered everytime in {@link net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit}
	 * @implNote {@code BEFORE_INIT} may be called before the callback list is emptied.
	 * @param callback the callback
	 */
	default void registerRecipeBookToggleCallback(Runnable callback) {
		throw new UnsupportedOperationException("Implemented via Mixin");
	}
}
