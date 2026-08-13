package de.hysky.skyblocker.skyblock;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;

public final class HideStatusEffectOverlay {
	@Init
	public static void init() {
		HudElementRegistry.replaceElement(VanillaHudElements.MOB_EFFECTS, hudElement -> {
			if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay) return (_, _) -> {};
			return hudElement;
		});
	}
}
