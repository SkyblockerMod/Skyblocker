package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FireFreezeStaffTimer {
    private static long fireFreezeTimer;

    public static void init() {
        HudRenderCallback.EVENT.register(FireFreezeStaffTimer::onDraw);
        ClientReceiveMessageEvents.ALLOW_GAME.register(FireFreezeStaffTimer::onChatMessage);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FireFreezeStaffTimer.reset());
    }

    private static void onDraw(DrawContext context, float v) {
        var client = MinecraftClient.getInstance();

        if (client.currentScreen != null) return;

        if (SkyblockerConfigManager.get().locations.dungeons.fireFreezeStaffTimer) {
            if (FireFreezeStaffTimer.fireFreezeTimer != 0) {
                var now = System.currentTimeMillis();

                if (now >= FireFreezeStaffTimer.fireFreezeTimer + 5000) {
                    FireFreezeStaffTimer.reset();
                    return;
                }

                var message =
                        FireFreezeStaffTimer.fireFreezeTimer > now
                                ? String.format("%.2f", (float) (FireFreezeStaffTimer.fireFreezeTimer - now) / 1000) + "s"
                                : "NOW";

                var renderer = client.textRenderer;
                var width = client.getWindow().getScaledWidth() / 2;
                var height = client.getWindow().getScaledHeight() / 2;

                context.drawCenteredTextWithShadow(
                        renderer, "Fire freeze in: " + message, width, height, 0xffffff);
            }
        }
    }

    private static void reset() {
        FireFreezeStaffTimer.fireFreezeTimer = 0;
    }

    private static boolean onChatMessage(Text text, boolean overlay) {
        if (!overlay) {
            if (Formatting.strip(text.getString())
                    .equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
                FireFreezeStaffTimer.fireFreezeTimer = System.currentTimeMillis() + 5000L;
            }
        }

        return true;
    }
}
