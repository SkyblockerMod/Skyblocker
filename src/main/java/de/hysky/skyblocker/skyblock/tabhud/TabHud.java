package de.hysky.skyblocker.skyblock.tabhud;

import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class TabHud {
	public static KeyMapping toggleSecondary;
	private static KeyMapping defaultTgl;

	@Init
	public static void init() {
		toggleSecondary = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.skyblocker.toggleA",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_Z,
						SkyblockerMod.KEYBINDING_CATEGORY));
		defaultTgl = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.skyblocker.defaultTgl",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_M,
						SkyblockerMod.KEYBINDING_CATEGORY));
	}

	public static boolean shouldRenderVanilla() {
		return defaultTgl.isDown() != SkyblockerConfigManager.get().uiAndVisuals.tabHud.showVanillaTabByDefault;
	}
}
