package de.hysky.skyblocker.utils;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;

public final class RegistryUtils {
	private static final HolderLookup.Provider LOOKUP = VanillaRegistries.createLookup();

	/**
	 * Tries to get the dynamic registry manager instance currently in use or else returns {@link #LOOKUP}
	 */
	public static HolderLookup.Provider getRegistryWrapperLookup() {
		Minecraft client = Minecraft.getInstance();
		// Null check on client for tests
		return client != null && client.getConnection() != null && client.getConnection().registryAccess() != null ? client.getConnection().registryAccess() : LOOKUP;
	}

	/**
	 * Gets the dynamic registry manager instance currently in use or else returns {@code null}.
	 */
	public static HolderLookup.@Nullable Provider getCurrentRegistryWrapperLookup() {
		Minecraft client = Minecraft.getInstance();
		// Null check on client for tests
		return client != null && client.getConnection() != null ? client.getConnection().registryAccess() : null;
	}
}
