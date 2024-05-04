package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WikiLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikiLookup.class);
    public static KeyBinding wikiLookup;

    public static void init() {
        wikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikiLookup",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                "key.categories.skyblocker"
        ));
    }

    public static void openWiki(Slot slot, PlayerEntity player) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) return;

        ItemUtils.getItemIdOptional(slot.getStack()).map(id -> ItemRepository.getWikiLink(id, player)).ifPresent(wikiLink -> {
            try {
                CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink));
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e);
                if (player != null) {
                    player.sendMessage(Constants.PREFIX.get().append("Error while retrieving wiki article, see logs..."), false);
                }
            }
        });
    }

}
