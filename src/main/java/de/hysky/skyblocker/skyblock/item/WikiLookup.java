package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiLookup {
	private static final Logger LOGGER = LoggerFactory.getLogger(WikiLookup.class);
	public static KeyBinding officialWikiLookup;
	public static KeyBinding fandomWikiLookup;

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

	public static void openWiki(@NotNull Slot slot, @NotNull PlayerEntity player, boolean useOfficial) {
		WikiLookup.openWiki(slot.getStack(), player, useOfficial);
	}

	public static void openWiki(ItemStack stack, PlayerEntity player, boolean useOfficial) {
		String neuId = ItemUtils.getItemId(stack);
		String wikiLink = ItemRepository.getWikiLink(neuId, useOfficial);

		if (wikiLink == null) {
			Text message = useOfficial ? Text.translatable("skyblocker.wikiLookup.noArticleFound.official") : Text.translatable("skyblocker.wikiLookup.noArticleFound.fandom");
			player.sendMessage(Constants.PREFIX.get().append(message), false);
			return;
		}

		try {
			Util.getOperatingSystem().open(wikiLink);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e);
			player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
		}
	}
}
