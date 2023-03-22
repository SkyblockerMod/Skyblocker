package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

public class WikiLookup {
    public static KeyBinding wikiLookup;
    static MinecraftClient client = MinecraftClient.getInstance();
    static String id;
    public static Gson gson = new Gson();

    public static void init(){
        wikiLookup = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikiLookup",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F4,
                "key.categories.skyblocker"
        ));
    }

    public static String getSkyblockId(Slot slot) {
        //Grabbing the skyblock NBT data
        ItemStack selectedStack = slot.getStack();
        NbtCompound nbt = selectedStack.getSubNbt("ExtraAttributes");
        if (nbt != null) {
            id = nbt.getString("id");
        }
        return id;
    }

    public static void openWiki(Slot slot) {
        if (Utils.isOnSkyblock) {
            id = getSkyblockId(slot);
            try {
                String wikiLink = ItemRegistry.getWikiLink(id);
                CompletableFuture.runAsync(() -> Util.getOperatingSystem().open(wikiLink));
            } catch (IndexOutOfBoundsException | IllegalStateException e) {
                e.printStackTrace();
                if (client.player != null)
                    client.player.sendMessage(Text.of("Error while retrieving wiki article..."), false);
            }
        }
    }

}
