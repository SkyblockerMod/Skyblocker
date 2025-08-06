package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.datafixers.util.Either;
import de.hysky.skyblocker.utils.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;

/**
 * An item wiki lookup for certain screen.
 * See {@link RegularItemLookup} for global item lookup.
 * @see VisitorLookup
 */
public interface WikiLookup {
	Map<String, String> CHARACTER_ENCODER = Map.ofEntries(
			Map.entry(" ", "_"),
			// Special case for only a visitor that has '?'
			Map.entry("?", encodeChar("?"))
	);
	Function<String, String> REPLACING_FUNCTION = CHARACTER_ENCODER.entrySet().stream()
			.map(entryToReplace -> (Function<String, String>) s -> s.replace(entryToReplace.getKey(), entryToReplace.getValue()))
			.reduce(Function.identity(), Function::andThen);

	/**
	 * Called after {@link WikiLookup#canSearch(String, Either)} when pressing a wiki lookup key.
	 * @param either Can be {@link Slot} or {@link ItemStack}.
	 * @param player The player entity.
	 * @param useOfficial Use official will open Hypixel Wiki, other than that will open Fandom.
	 */
	void open(@NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial);

	/**
	 * Called before open the wiki lookup.
	 * @param title Screen title name.
	 * @param either Can be {@link Slot} or {@link ItemStack}.
	 * @return {@code true} if it can look up wiki on the screen, {@code false} otherwise. Default is {@code true}.
	 */
	default boolean canSearch(@Nullable String title, @NotNull Either<Slot, ItemStack> either) {
		return true;
	}

	static void openWikiLink(String wikiLink, PlayerEntity player) {
		CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink)).exceptionally(e -> {
			WikiLookupManager.LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e);
			player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
			return null;
		});
	}

	private static String encodeChar(String ch) {
		return URLEncoder.encode(ch, StandardCharsets.UTF_8);
	}
}
