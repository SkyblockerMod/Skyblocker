package de.hysky.skyblocker.compatibility.jei;

import net.fabricmc.loader.api.FabricLoader;

/*
 * Used for JEI compatibility to avoid crashes with the main class since the JEI interface won't exist if JEI is not present.
 */
public class JEICompatibility {
	public static final boolean JEI_LOADED = FabricLoader.getInstance().isModLoaded("jei");
}
