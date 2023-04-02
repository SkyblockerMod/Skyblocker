package me.xmrvizzy.skyblocker.skyblock.tabhud;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class TabHud {

    public static KeyBinding playerTgl;
    public static KeyBinding genericTgl;
    // public static KeyBinding mapTgl;
    public static KeyBinding defaultTgl;

    public static void init() {

        playerTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyhytab.playerTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_LEFT_SHIFT,
                        "key.categories.skyblocker"));
        genericTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.tabhud.genericTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_LEFT_ALT,
                        "key.categories.skyblocker"));
        // mapTgl = KeyBindingHelper.registerKeyBinding(
        //         new KeyBinding("key.tabhud.mapTgl",
        //                 InputUtil.Type.KEYSYM,
        //                 GLFW.GLFW_KEY_LEFT_ALT,
        //                 "key.categories.skyblocker"));
        defaultTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.tabhud.defaultTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        "key.categories.skyblocker"));

    }
}
