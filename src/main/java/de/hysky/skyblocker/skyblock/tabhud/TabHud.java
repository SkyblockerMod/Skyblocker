package de.hysky.skyblocker.skyblock.tabhud;

import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class TabHud {
	public static KeyMapping toggleSecondary;
	private static KeyMapping defaultTgl;

	@Init
	public static void init() {
		toggleSecondary = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.skyblocker.toggleA",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_Z,
						SkyblockerMod.KEYBINDING_CATEGORY));
		defaultTgl = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key.skyblocker.defaultTgl",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_M,
						SkyblockerMod.KEYBINDING_CATEGORY));

		HudElementRegistry.replaceElement(VanillaHudElements.PLAYER_LIST, hudElement -> {
			if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.shouldRenderVanilla() || Minecraft.getInstance().screen instanceof WidgetsConfigurationScreen) return hudElement;
			return (_, _) -> {};
		});
	}

	public static boolean shouldRenderVanilla() {
		return defaultTgl.isDown() != SkyblockerConfigManager.get().uiAndVisuals.tabHud.showVanillaTabByDefault;
	}
}
