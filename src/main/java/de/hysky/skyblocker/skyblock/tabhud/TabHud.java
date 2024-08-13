package de.hysky.skyblocker.skyblock.tabhud;

import de.hysky.skyblocker.annotations.Init;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class TabHud {

    public static KeyBinding toggleB;
    public static KeyBinding toggleA;
    // public static KeyBinding mapTgl;
    public static KeyBinding defaultTgl;

    public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Tab HUD");

    @Init
    public static void init() {

        toggleB = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.toggleB",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_B,
                        "key.categories.skyblocker"));
        toggleA = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.toggleA",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_N,
                        "key.categories.skyblocker"));
        defaultTgl = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.skyblocker.defaultTgl",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        "key.categories.skyblocker"));

    }
}
