package me.xmrvizzy.skyblocker.skyblock.item;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
                GLFW.GLFW_KEY_Y,
                "key.categories.skyblocker"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (wikiLookup.wasPressed()) {
                id = getSkyblockId();

                try {
                    //Setting up a connection with the repo
                    String urlString = "https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + id + ".json";
                    URL url = new URL(urlString);
                    URLConnection request = url.openConnection();
                    request.connect();

                    //yoinking the wiki link
                    JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
                    JsonObject rootobj = root.getAsJsonObject();
                    String wikiLink = rootobj.get("info").getAsString();
                    Util.getOperatingSystem().open(wikiLink);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static String getSkyblockId() {

        //Grabbing the skyblock NBT data
        ItemStack mainHandStack = client.player.getMainHandStack();
        NbtCompound nbt = mainHandStack.getSubNbt("ExtraAttributes");
        if (nbt != null) {
            id = nbt.getString("id");
        }

        return id;
    }

}
