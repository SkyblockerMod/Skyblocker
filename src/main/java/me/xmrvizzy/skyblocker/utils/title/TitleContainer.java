package me.xmrvizzy.skyblocker.utils.title;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHudConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class TitleContainer {
    public static final MinecraftClient client = MinecraftClient.getInstance();
    public static List<Title> titles = new ArrayList<>();

    public static void init() {
        HudRenderCallback.EVENT.register(TitleContainer::draw);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("titleContainer")
                                .executes(context -> {
                                    client.send(() -> client.setScreen(new TitleContainerConfigScreen(Text.of("Title Container HUD Config"))));
                                    return 1;
                                })))));
    }
    public static void addTitle(Title title) {
        title.active = true;
        title.lastX = 0;
        title.lastY = SkyblockerConfig.get().general.titleContainer.y;
        titles.add(title);
    }
    public static void addTitleWithDismiss(Title title, int ticks) {
        addTitle(title);
        SkyblockerMod.getInstance().scheduler.schedule(() -> {
            title.active = false;
        }, ticks);
    }
    public static void draw(DrawContext context, float tickDelta) {
        draw(titles, SkyblockerConfig.get().general.titleContainer.x, SkyblockerConfig.get().general.titleContainer.y, context, tickDelta, false);
    }
    public static void draw(List<Title> titlesToDraw, int xPos, int yPos, DrawContext context, float tickDelta, boolean example) {
        var client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        List<Title> toRemove = new ArrayList<>();

        float scale = 3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F);

        var direction = SkyblockerConfig.get().general.titleContainer.direction;
        var alignment = SkyblockerConfig.get().general.titleContainer.alignment;
        float x = 0;
        float y;
        float width = 0;
        for (Title title : titlesToDraw) {
            width += textRenderer.getWidth(title.text) * scale + 10;
        }
        if(direction == SkyblockerConfig.Direction.HORIZONTAL) {
            if (alignment == SkyblockerConfig.Alignment.MIDDLE) {
                x = xPos - (width / 2);
            }
        } else {
            if (alignment == SkyblockerConfig.Alignment.MIDDLE) {
                x = xPos;
            }
        }
        if(alignment == SkyblockerConfig.Alignment.LEFT || alignment == SkyblockerConfig.Alignment.RIGHT) {
            x = xPos;
        }
        y = yPos;

        for (Title title : titlesToDraw) {
            context.getMatrices().push();
            context.getMatrices().translate(title.lastX, title.lastY, 200);
            context.getMatrices().scale(scale, scale, scale);

            float xToUse = 0;
            if(direction == SkyblockerConfig.Direction.HORIZONTAL) {
                xToUse = alignment == SkyblockerConfig.Alignment.RIGHT ?
                        x - (textRenderer.getWidth(title.text) * scale) :
                        x;
            } else {
                xToUse = alignment == SkyblockerConfig.Alignment.MIDDLE ?
                        x - (textRenderer.getWidth(title.text) * scale) / 2 :
                        alignment == SkyblockerConfig.Alignment.RIGHT ?
                                x - (textRenderer.getWidth(title.text) * scale) :
                                x;
            }
            title.lastX = MathHelper.lerp(tickDelta * 0.5F, title.lastX, xToUse);
            title.lastY = MathHelper.lerp(tickDelta * 0.5F, title.lastY, y);

            if(direction == SkyblockerConfig.Direction.HORIZONTAL) {
                if (alignment == SkyblockerConfig.Alignment.MIDDLE || alignment == SkyblockerConfig.Alignment.LEFT) {
                    x += textRenderer.getWidth(title.text) * scale + 10;
                }

                if (alignment == SkyblockerConfig.Alignment.RIGHT) {
                    x -= textRenderer.getWidth(title.text) * scale + 10;
                }
            } else {
                y += textRenderer.fontHeight * scale + 10;
            }

            context.drawText(textRenderer, title.text, 0, 0, title.color, true);
            context.getMatrices().pop();
            if (!title.active && !example) {
                toRemove.add(title);
            }
        }
        if (!example) {
            titlesToDraw.removeAll(toRemove);
        }
    }
}