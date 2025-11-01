package de.hysky.skyblocker.skyblock.tabhud;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TabHud {
	public static KeyBinding toggleSecondary;
	private static KeyBinding defaultTgl;

	@Init
	public static void init() {
		toggleSecondary = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.toggleA",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_Z,
						SkyblockerMod.KEYBINDING_CATEGORY));
		defaultTgl = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.skyblocker.defaultTgl",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_M,
						SkyblockerMod.KEYBINDING_CATEGORY));
	}

	public static boolean shouldRenderVanilla() {
		return defaultTgl.isPressed() != SkyblockerConfigManager.get().uiAndVisuals.tabHud.showVanillaTabByDefault;
	}
}
