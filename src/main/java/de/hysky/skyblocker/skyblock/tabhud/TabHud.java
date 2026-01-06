package de.hysky.skyblocker.skyblock.tabhud;

import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

		HudElementRegistry.replaceElement(VanillaHudElements.PLAYER_LIST, hudElement -> {
			if (!Utils.isOnSkyblock() || TabHud.shouldRenderVanilla() || !WidgetManager.getScreenBuilder(Utils.getLocation(), WidgetManager.ScreenLayer.MAIN_TAB).hasFancyTabWidget) return hudElement;
			return (context, tickCounter) -> {};
		});
	}

	public static boolean shouldRenderVanilla() {
		return defaultTgl.isDown() != SkyblockerConfigManager.get().uiAndVisuals.hud.showVanillaTabByDefault;
	}

	public static float getScaleFactor() {
		return SkyblockerConfigManager.get().uiAndVisuals.hud.hudScale / 100f;
	}

	public static int getHudWidth() {
		return (int) (Minecraft.getInstance().getWindow().getGuiScaledWidth() / getScaleFactor());
	}

	public static int getHudHeight() {
		return (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() / getScaleFactor());
	}
}
