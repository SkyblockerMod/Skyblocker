package de.hysky.skyblocker.skyblock.item.wikilookup;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class WikiLookupManager {
	public static final Logger LOGGER = LogUtils.getLogger();

	public static KeyBinding officialWikiLookup;
	public static KeyBinding fandomWikiLookup;

	private static final WikiLookup[] LOOKUPS = new WikiLookup[] {
			VisitorLookup.INSTANCE,
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
		return (officialWikiLookup.getBoundKeyLocalizedText().getString() + "/" + fandomWikiLookup.getBoundKeyLocalizedText().getString()).toUpperCase(Locale.ENGLISH);
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
				lookup.open(either, player, useOfficial);
				break;
			}
		}
	}
}
