package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class WikiLookupManager {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static KeyMapping officialWikiLookup;
	public static KeyMapping fandomWikiLookup;

	private static final WikiLookup[] LOOKUPS = new WikiLookup[] {
			VisitorLookup.INSTANCE,
			PetItemLookup.INSTANCE,
			EnchantmentBookItemLookup.INSTANCE,
			EventCalendarLookup.INSTANCE,
			// Always in the last
			RegularItemLookup.INSTANCE
	};

	@Init
	public static void init() {
		officialWikiLookup = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.skyblocker.wikiLookup.official",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				SkyblockerMod.KEYBINDING_CATEGORY
		));

		fandomWikiLookup = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.skyblocker.wikiLookup.fandom",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_F1,
				SkyblockerMod.KEYBINDING_CATEGORY
		));
	}

	public static String getKeysText() {
		String official = officialWikiLookup.isUnbound() ? null : officialWikiLookup.getTranslatedKeyMessage().getString();
		String fandom = fandomWikiLookup.isUnbound() ? null : fandomWikiLookup.getTranslatedKeyMessage().getString();

		if (official == null && fandom == null) return "";
		if (official == null) return fandom.toUpperCase(Locale.ENGLISH);
		if (fandom == null) return official.toUpperCase(Locale.ENGLISH);

		return (official + "/" + fandom).toUpperCase(Locale.ENGLISH);
	}

	public static boolean handleWikiLookup(Either<Slot, ItemStack> either, Player player, KeyEvent input) {
		return handleWikiLookup(null, either, player, input);
	}

	public static boolean handleWikiLookup(@Nullable String title, Either<Slot, ItemStack> either, Player player, KeyEvent input) {
		if (SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) {
			boolean official = officialWikiLookup.matches(input);
			if (official || fandomWikiLookup.matches(input)) {
				openWiki(title, either, player, official);
				return true;
			}
		}
		return false;
	}

	public static void openWiki(ItemStack itemStack, Player player, boolean useOfficial) {
		openWiki(null, Either.right(itemStack), player, useOfficial);
	}

	public static void openWiki(@Nullable String title, Either<Slot, ItemStack> either, Player player, boolean useOfficial) {
		for (WikiLookup lookup : LOOKUPS) {
			if (lookup.canSearch(title, either)) {
				ItemStack itemStack = mapEitherToItemStack(either);
				lookup.open(itemStack, player, useOfficial);
				break;
			}
		}
	}

	public static void openWikiLinkName(String name, Player player, boolean useOfficial) {
		String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + name;
		openWikiLink(wikiLink, player);
	}

	public static void openWikiLink(String wikiLink, Player player) {
		CompletableFuture.runAsync(() -> Util.getPlatform().openUri(wikiLink), Executors.newVirtualThreadPerTaskExecutor()).exceptionally(e -> {
			WikiLookupManager.LOGGER.error("[Skyblocker] Error while retrieving wiki article: {}", wikiLink, e);
			player.displayClientMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
			return null;
		});
	}

	public static ItemStack mapEitherToItemStack(Either<Slot, ItemStack> either) {
		return either.right().orElseGet(() -> either.mapLeft(Slot::getItem).left().orElse(ItemStack.EMPTY));
	}
}
