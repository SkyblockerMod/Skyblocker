package de.hysky.skyblocker.skyblock.tabhud.hud;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hud {

    public static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker HUD");

    public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static void init() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> render(context));
    }

    public static void render(DrawContext context) {
        if (CLIENT.options.playerListKey.isPressed() || CLIENT.player == null || CLIENT.getNetworkHandler() == null)
            return;

        float scale = SkyblockerConfigManager.get().general.tabHud.tabHudScale / 100f;
        int w = (int) (CLIENT.getWindow().getScaledWidth() / scale);
        int h = (int) (CLIENT.getWindow().getScaledHeight() / scale);

        try {
            HudScreenMaster.render(context, w, h);
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Encountered unknown exception while drawing default hud", e);
        }
    }
}
