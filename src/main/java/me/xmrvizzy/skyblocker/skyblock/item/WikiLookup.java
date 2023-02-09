package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
                //Setting up a connection with the repo
                String urlString = "https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + id + ".json";
                URL url = new URL(urlString);
                URLConnection request = url.openConnection();
                request.connect();

                //yoinking the wiki link
                JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
                JsonObject rootobj = root.getAsJsonObject();
                String wikiLink = rootobj.get("info").getAsJsonArray().get(1).getAsString();
                Util.getOperatingSystem().open(wikiLink);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                client.player.sendMessage(Text.of("Can't locate a wiki article for this item..."), false);
            } catch (ArrayIndexOutOfBoundsException | IllegalStateException e) {
                e.printStackTrace();
                client.player.sendMessage(Text.of("Error while retrieving wiki article..."), false);
            }
        }
    }

}
