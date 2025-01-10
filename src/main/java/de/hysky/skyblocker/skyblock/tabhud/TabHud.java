package de.hysky.skyblocker.skyblock.tabhud;

import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TabHud {
	public static KeyBinding toggleSecondary;
	public static KeyBinding defaultTgl;

	@Init
	public static void init() {
		toggleSecondary = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.toggleA",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_Z,
						"key.categories.skyblocker"));
		defaultTgl = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.defaultTgl",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_M,
						"key.categories.skyblocker"));
	}
}
