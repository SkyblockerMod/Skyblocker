package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;

public final class WikiLookupManager {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static KeyBinding officialWikiLookup;
	public static KeyBinding fandomWikiLookup;

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
		officialWikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.wikiLookup.official",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F4,
				"key.categories.skyblocker"
		));

		fandomWikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.wikiLookup.fandom",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F1,
				"key.categories.skyblocker"
		));
	}

	public static String getKeysText() {
		String official = officialWikiLookup.isUnbound() ? null : officialWikiLookup.getBoundKeyLocalizedText().getString();
		String fandom = fandomWikiLookup.isUnbound() ? null : fandomWikiLookup.getBoundKeyLocalizedText().getString();

		if (official == null && fandom == null) return "";
		if (official == null) return fandom.toUpperCase(Locale.ENGLISH);
		if (fandom == null) return official.toUpperCase(Locale.ENGLISH);

		return (official + "/" + fandom).toUpperCase(Locale.ENGLISH);
	}

	public static boolean handleWikiLookup(@NotNull Either<Slot, ItemStack> either, PlayerEntity player, int keyCode, int scanCode) {
		return handleWikiLookup(null, either, player, keyCode, scanCode);
	}

	public static boolean handleWikiLookup(@Nullable String title, @NotNull Either<Slot, ItemStack> either, PlayerEntity player, int keyCode, int scanCode) {
		if (SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) {
			boolean official = officialWikiLookup.matchesKey(keyCode, scanCode);
			if (official || fandomWikiLookup.matchesKey(keyCode, scanCode)) {
				openWiki(title, either, player, official);
				return true;
			}
		}
		return false;
	}

	public static void openWiki(@NotNull ItemStack itemStack, @NotNull PlayerEntity player, boolean useOfficial) {
		openWiki(null, Either.right(itemStack), player, useOfficial);
	}

	public static void openWiki(@Nullable String title, @NotNull Either<Slot, ItemStack> either, @NotNull PlayerEntity player, boolean useOfficial) {
		for (WikiLookup lookup : LOOKUPS) {
			if (lookup.canSearch(title, either)) {
				ItemStack itemStack = mapEitherToItemStack(either);
				lookup.open(itemStack, player, useOfficial);
				break;
			}
		}
	}

	public static void openWikiLinkName(String name, @NotNull PlayerEntity player, boolean useOfficial) {
		String wikiLink = ItemRepository.getWikiLink(useOfficial) + "/" + name;
		openWikiLink(wikiLink, player);
	}

	public static void openWikiLink(String wikiLink, PlayerEntity player) {
		CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink)).exceptionally(e -> {
			WikiLookupManager.LOGGER.error("[Skyblocker] Error while retrieving wiki article: {}", wikiLink, e);
			player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
			return null;
		});
	}

	public static ItemStack mapEitherToItemStack(Either<Slot, ItemStack> either) {
		return either.right().orElseGet(() -> either.mapLeft(Slot::getStack).left().orElse(ItemStack.EMPTY));
	}
}
