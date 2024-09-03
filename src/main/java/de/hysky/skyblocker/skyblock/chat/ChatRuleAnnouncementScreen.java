package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ChatRuleAnnouncementScreen {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static float timer;
    private static Text text = null;

    @Init
    public static void init() {
        HudRenderEvents.BEFORE_CHAT.register((context, tickCounter) -> {
            if (timer <= 0 || text == null) {
                return;
            }
            render(context, tickCounter);
        });
    }

    /**
     * renders {@link ChatRuleAnnouncementScreen#text} to the middle of the top of the screen.
     * @param context render context
     * @param tickCounter difference from last render to remove from timer
     */
    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        int scale = SkyblockerConfigManager.get().chat.chatRuleConfig.announcementScale;
        //decrement timer
        timer -= tickCounter.getTickDelta(true);
        //scale text up and center
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() * 0.3, 0f);
        matrices.scale(scale, scale, 0f);
        //render text
        context.drawCenteredTextWithShadow(CLIENT.textRenderer, text, 0, 0, 0xFFFFFFFF);

        matrices.pop();
    }

    protected static void setText(Text newText) {
        text = newText;
        timer =  SkyblockerConfigManager.get().chat.chatRuleConfig.announcementLength;
    }
}