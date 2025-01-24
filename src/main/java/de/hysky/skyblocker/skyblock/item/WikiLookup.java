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

import java.util.concurrent.CompletableFuture;

public class WikiLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikiLookup.class);
    public static KeyBinding wikiLookup;

    @Init
    public static void init() {
        wikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikiLookup",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                "key.categories.skyblocker"
        ));
    }

    public static void openWiki(@NotNull Slot slot, @NotNull PlayerEntity player) {
        WikiLookup.openWiki(slot.getStack(), player);
    }

	public static void openWiki(ItemStack stack, PlayerEntity player) {
		ItemUtils.getItemIdOptional(stack)
				.map(ItemRepository::getWikiLink)
				.ifPresentOrElse(wikiLink -> CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink)).exceptionally(e -> {
					LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e);
					player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
					return null;
				}), () -> player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.wikiLookup.noArticleFound")), false));
	}
}
