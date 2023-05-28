package me.xmrvizzy.skyblocker.skyblock.tabhud;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class TabHud {

    public static KeyBinding playerTgl;
    public static KeyBinding genericTgl;
    // public static KeyBinding mapTgl;
    public static KeyBinding defaultTgl;

    public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Tab HUD");

    public static void init() {

        playerTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.playerTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        "key.categories.skyblocker"));
        genericTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.genericTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_N,
                        "key.categories.skyblocker"));
        // mapTgl = KeyBindingHelper.registerKeyBinding(
        //         new KeyBinding("key.tabhud.mapTgl",
        //                 InputUtil.Type.KEYSYM,
        //                 GLFW.GLFW_KEY_LEFT_ALT,
        //                 "key.categories.skyblocker"));
        defaultTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.defaultTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        "key.categories.skyblocker"));

    }
}
