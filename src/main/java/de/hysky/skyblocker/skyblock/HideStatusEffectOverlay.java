package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class HideStatusEffectOverlay {
	@Init
	public static void init() {
		HudElementRegistry.replaceElement(VanillaHudElements.STATUS_EFFECTS, hudElement -> {
			if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay) return (context, tickCounter) -> {};
			return hudElement;
		});
	}
}
