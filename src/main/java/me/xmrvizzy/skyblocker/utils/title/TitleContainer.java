package me.xmrvizzy.skyblocker.utils.title;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class TitleContainer {
    public static List<Title> titles = new ArrayList<>();

    public static void init() {
        HudRenderCallback.EVENT.register(TitleContainer::draw);
    }
    public static void addTitle(Title title) {
        title.active = true;
        title.lastX = 0;
        titles.add(title);
    }
    public static void draw(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        List<Title> toRemove = new ArrayList<>();

        float scale = 3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F);

        float x;
        float width = 0;
        for (Title title : titles) {
            width += textRenderer.getWidth(title.text) * scale + 10;
        }

        x = (client.getWindow().getScaledWidth() / 2) - width / 2;

        for (Title title : titles) {
            context.getMatrices().push();
            context.getMatrices().translate(title.lastX, client.getWindow().getScaledHeight() * 0.7F, 0);
            context.getMatrices().scale(scale, scale, scale);

            title.lastX = MathHelper.lerp(tickDelta * 0.5F, title.lastX, x);
            x += textRenderer.getWidth(title.text) * scale + 10;
            context.drawText(textRenderer, title.text, 0, 0, title.color, true);
            context.getMatrices().pop();
            if (!title.active) {
                toRemove.add(title);
            }
        }
        titles.removeAll(toRemove);
    }
}