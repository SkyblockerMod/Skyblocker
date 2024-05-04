package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WikiLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikiLookup.class);
    public static KeyBinding wikiLookup;
    private static String id;

    public static void init() {
        wikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikiLookup",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                "key.categories.skyblocker"
        ));
    }

    public static void getSkyblockId(Slot slot) {
        //Grabbing the skyblock NBT data
    	//Setting to null prevents a bug where using wiki lookup on an empty slot would bring up the last looked up page
        id = ItemUtils.getItemIdOptional(slot.getStack()).orElse(null);
    }

    public static void openWiki(Slot slot, PlayerEntity player) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.wikiLookup.enableWikiLookup) {
            getSkyblockId(slot);

            //Fixes a crash where using wiki lookup for the first time on an item that doesn't have an id (because id is initialized to null)
            //and now also because we set it to null above for said reason
            if (id != null) {
                try {
                    String wikiLink = ItemRepository.getWikiLink(id, player);
                    if (wikiLink != null) CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink));
                } catch (IndexOutOfBoundsException | IllegalStateException e) {
                    LOGGER.error("[Skyblocker] Error while retrieving wiki article...", e);
                    if (player != null) {
                        player.sendMessage(Text.of("[Skyblocker] Error while retrieving wiki article, see logs..."), false);
                    }
                }
            }
        }
    }

}
