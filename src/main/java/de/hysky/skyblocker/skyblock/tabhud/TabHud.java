package de.hysky.skyblocker.skyblock.tabhud;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
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

		HudElementRegistry.replaceElement(VanillaHudElements.PLAYER_LIST, hudElement -> {
			if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.shouldRenderVanilla() || MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) return hudElement;
			return (context, tickCounter) -> {};
		});
	}

	public static boolean shouldRenderVanilla() {
		return defaultTgl.isPressed() != SkyblockerConfigManager.get().uiAndVisuals.tabHud.showVanillaTabByDefault;
	}
}
