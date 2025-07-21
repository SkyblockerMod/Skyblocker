package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class FireFreezeStaffTimer {
	private static final Identifier FIRE_FREEZE_STAFF_TIMER = Identifier.of("skyblocker", "fire_freeze_staff_timer");
    private static long fireFreezeTimer;

    @Init
    public static void init() {
		HudElementRegistry.attachElementAfter(VanillaHudElements.OVERLAY_MESSAGE, FIRE_FREEZE_STAFF_TIMER, FireFreezeStaffTimer::onDraw);
        ClientReceiveMessageEvents.ALLOW_GAME.register(FireFreezeStaffTimer::onChatMessage);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FireFreezeStaffTimer.reset());
    }

    private static void onDraw(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.currentScreen != null) return;

        if (SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && fireFreezeTimer != 0) {
            long now = System.currentTimeMillis();

            if (now >= fireFreezeTimer + 5000) {
                reset();
                return;
            }

            String message =
                    fireFreezeTimer > now
                            ? String.format("%.2f", (float) (fireFreezeTimer - now) / 1000) + "s"
                            : "NOW";

            TextRenderer renderer = client.textRenderer;
            int width = client.getWindow().getScaledWidth() / 2;
            int height = client.getWindow().getScaledHeight() / 2;

            context.drawCenteredTextWithShadow(
                    renderer, "Fire freeze in: " + message, width, height, Colors.WHITE);
        }
    }

    private static void reset() {
        fireFreezeTimer = 0;
    }

    private static boolean onChatMessage(Text text, boolean overlay) {
        if (!overlay && SkyblockerConfigManager.get().dungeons.theProfessor.fireFreezeStaffTimer && Formatting.strip(text.getString())
                .equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
            fireFreezeTimer = System.currentTimeMillis() + 5000L;
        }

        return true;
    }
}
