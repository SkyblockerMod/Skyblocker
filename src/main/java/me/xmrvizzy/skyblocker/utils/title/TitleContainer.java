package me.xmrvizzy.skyblocker.utils.title;

import com.mojang.brigadier.Command;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashSet;
import java.util.Set;

public class TitleContainer {
    /**
     * The set of titles which will be rendered.
     *
     * @see #containsTitle(Title)
     * @see #addTitle(Title)
     * @see #addTitle(Title, int)
     * @see #removeTitle(Title)
     */
    private static final Set<Title> titles = new LinkedHashSet<>();

    public static void init() {
        HudRenderCallback.EVENT.register(TitleContainer::render);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.literal("titleContainer")
                                .executes(context -> {
                                    SkyblockerMod.getInstance().scheduler.queueOpenScreen(new TitleContainerConfigScreen(Text.of("Title Container HUD Config")));
                                    return Command.SINGLE_SUCCESS;
                                })))));
    }

    /**
     * Returns {@code true} if the title is currently shown.
     *
     * @param title the title to check
     * @return whether the title in currently shown
     */
    public static boolean containsTitle(Title title) {
        return titles.contains(title);
    }

    /**
     * Adds a title to be shown
     *
     * @param title the title to be shown
     * @return whether the title is already currently being shown
     */
    public static boolean addTitle(Title title) {
        if (titles.add(title)) {
            title.lastX = 0;
            title.lastY = SkyblockerConfig.get().general.titleContainer.y;
            return true;
        }
        return false;
    }

    /**
     * Adds a title to be shown for a set number of ticks
     *
     * @param title the title to be shown
     * @param ticks the number of ticks to show the title
     * @return whether the title is already currently being shown
     */
    public static boolean addTitle(Title title, int ticks) {
        if (addTitle(title)) {
            SkyblockerMod.getInstance().scheduler.schedule(() -> TitleContainer.removeTitle(title), ticks);
            return true;
        }
        return false;
    }

    /**
     * Stops showing a title
     *
     * @param title the title to stop showing
     */
    public static void removeTitle(Title title) {
        titles.remove(title);
    }

    private static void render(DrawContext context, float tickDelta) {
        render(context, titles, SkyblockerConfig.get().general.titleContainer.x, SkyblockerConfig.get().general.titleContainer.y, tickDelta);
    }

    protected static void render(DrawContext context, Set<Title> titles, int xPos, int yPos, float tickDelta) {
        var client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        float scale = 3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F);

        SkyblockerConfig.Direction direction = SkyblockerConfig.get().general.titleContainer.direction;
        SkyblockerConfig.Alignment alignment = SkyblockerConfig.get().general.titleContainer.alignment;
        float x = 0;
        float y;
        float width = 0;
        for (Title title : titles) {
            width += textRenderer.getWidth(title.getText()) * scale + 10;
        }
        if (direction == SkyblockerConfig.Direction.HORIZONTAL) {
            if (alignment == SkyblockerConfig.Alignment.MIDDLE) {
                x = xPos - (width / 2);
            }
        } else {
            if (alignment == SkyblockerConfig.Alignment.MIDDLE) {
                x = xPos;
            }
        }
        if (alignment == SkyblockerConfig.Alignment.LEFT || alignment == SkyblockerConfig.Alignment.RIGHT) {
            x = xPos;
        }
        y = yPos;

        for (Title title : titles) {
            context.getMatrices().push();
            context.getMatrices().translate(title.lastX, title.lastY, 200);
            context.getMatrices().scale(scale, scale, scale);

            float xToUse;
            if (direction == SkyblockerConfig.Direction.HORIZONTAL) {
                xToUse = alignment == SkyblockerConfig.Alignment.RIGHT ?
                        x - (textRenderer.getWidth(title.getText()) * scale) :
                        x;
            } else {
                xToUse = alignment == SkyblockerConfig.Alignment.MIDDLE ?
                        x - (textRenderer.getWidth(title.getText()) * scale) / 2 :
                        alignment == SkyblockerConfig.Alignment.RIGHT ?
                                x - (textRenderer.getWidth(title.getText()) * scale) :
                                x;
            }
            title.lastX = MathHelper.lerp(tickDelta * 0.5F, title.lastX, xToUse);
            title.lastY = MathHelper.lerp(tickDelta * 0.5F, title.lastY, y);

            if (direction == SkyblockerConfig.Direction.HORIZONTAL) {
                if (alignment == SkyblockerConfig.Alignment.MIDDLE || alignment == SkyblockerConfig.Alignment.LEFT) {
                    x += textRenderer.getWidth(title.getText()) * scale + 10;
                }

                if (alignment == SkyblockerConfig.Alignment.RIGHT) {
                    x -= textRenderer.getWidth(title.getText()) * scale + 10;
                }
            } else {
                y += textRenderer.fontHeight * scale + 10;
            }

            context.drawTextWithShadow(textRenderer, title.getText(), 0, 0, 0xFFFFFF);
            context.getMatrices().pop();
        }
    }
}