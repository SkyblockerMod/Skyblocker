package de.hysky.skyblocker.injected;

public interface RecipeBookHolder {

	/**
	 * Register a callback that gets called when the recipe book button is toggled.
	 * The callback list is emptied after each init, so this needs to be registered everytime in {@link net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit}
	 *
	 * @param callback the callback
	 * @implNote {@code BEFORE_INIT} may be called before the callback list is emptied.
	 */
	void registerRecipeBookToggleCallback(Runnable callback);
}
