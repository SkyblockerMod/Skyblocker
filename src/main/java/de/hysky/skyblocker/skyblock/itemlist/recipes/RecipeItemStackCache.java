package de.hysky.skyblocker.skyblock.itemlist.recipes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.hysky.skyblocker.utils.FlexibleItemStack;

/// Small helper class for optimizing recipe item stack creation by reusing instances for ingredients where possible.
public final class RecipeItemStackCache {
	public static final Map<String, FlexibleItemStack> CACHE = new ConcurrentHashMap<>();

	public static String getCacheKey(String id, int count) {
		return id + "+" + count;
	}
}
