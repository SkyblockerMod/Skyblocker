package me.xmrvizzy.skyblocker.utils.title;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class TitleContainerConfigScreen extends Screen {

    private final Title example1 = new Title("Test1", Formatting.RED.getColorValue());
    private final Title example2 = new Title("Test23", Formatting.AQUA.getColorValue());
    private final Title example3 = new Title("Testing1234", Formatting.DARK_GREEN.getColorValue());
    private int hudX = SkyblockerConfig.get().general.titleContainer.x;
    private int hudY = SkyblockerConfig.get().general.titleContainer.y;
    protected TitleContainerConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context);
        TitleContainer.draw(List.of(example1, example2, example3), hudX, hudY, context, delta, true);
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
        var direction = SkyblockerConfig.get().general.titleContainer.direction;
        var alignment = SkyblockerConfig.get().general.titleContainer.alignment;
        int width1 = client.textRenderer.getWidth("Press Q/E to change Alignment: " + alignment.toString());
        int width2 = client.textRenderer.getWidth("Press R to change Direction: " + direction.toString());
        context.drawText(client.textRenderer, "Press Q/E to change Alignment: " + alignment.toString(), (width / 2) - width1 / 2, client.textRenderer.fontHeight * 2, Color.GRAY.getRGB(), true);
        context.drawText(client.textRenderer, "Press R to change Direction: " + direction.toString(), (width / 2) - width2 / 2, client.textRenderer.fontHeight * 3 + 5, Color.GRAY.getRGB(), true);

        int midWidth = getSelectionWidth() / 2;
        int midHeight = getSelectionHeight() / 2;
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        if(direction == SkyblockerConfig.Direction.HORIZONTAL) {
            switch (alignment) {
                case RIGHT:
                    x1 = hudX - midWidth * 2;
                    x2 = hudX;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case MIDDLE:
                    x1 = hudX - midWidth;
                    x2 = hudX + midWidth;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case LEFT:
                    x1 = hudX;
                    x2 = hudX + midWidth * 2;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
            }
        } else {
            switch (alignment) {
                case RIGHT:
                    x1 = hudX - midWidth * 2;
                    x2 = hudX;
                    y1 = hudY;
                    y2 = hudY + midHeight;
                    break;
                case MIDDLE:
                    x1 = hudX - midWidth;
                    x2 = hudX + midWidth;
                    y1 = hudY;
                    y2 = hudY + midHeight;
                    break;
                case LEFT:
                    x1 = hudX;
                    x2 = hudX + midWidth * 2;
                    y1 = hudY;
                    y2 = hudY + midHeight;
                    break;
            }
        }
        context.drawHorizontalLine(x1, x2, y1, Color.RED.getRGB());
        context.drawHorizontalLine(x1, x2, y2, Color.RED.getRGB());
        context.drawVerticalLine(x1, y1, y2, Color.RED.getRGB());
        context.drawVerticalLine(x2, y1, y2, Color.RED.getRGB());
    }

    public int getSelectionHeight()
    {
        int scale = (int) (3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfig.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                textRenderer.fontHeight * scale :
                (textRenderer.fontHeight + 10) * 3 * scale;
    }

    public int getSelectionWidth()
    {
        int scale = (int) (3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfig.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                (textRenderer.getWidth("Test1") + 10 + textRenderer.getWidth("Test23") + 10 + textRenderer.getWidth("Testing1234")) * scale :
                textRenderer.getWidth("Testing1234") * scale;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int midWidth = getSelectionWidth() / 2;
        int midHeight = getSelectionHeight() / 2;
        var direction = SkyblockerConfig.get().general.titleContainer.direction;
        var alignment = SkyblockerConfig.get().general.titleContainer.alignment;
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        if(direction == SkyblockerConfig.Direction.HORIZONTAL) {
            switch (alignment) {
                case RIGHT:
                    x1 = hudX - midWidth * 2;
                    x2 = hudX;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case MIDDLE:
                    x1 = hudX - midWidth;
                    x2 = hudX + midWidth;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case LEFT:
                    x1 = hudX;
                    x2 = hudX + midWidth * 2;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
            }
        } else {
            switch (alignment) {
                case RIGHT:
                    x1 = hudX - midWidth * 2;
                    x2 = hudX;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case MIDDLE:
                    x1 = hudX - midWidth;
                    x2 = hudX + midWidth;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
                case LEFT:
                    x1 = hudX;
                    x2 = hudX + midWidth * 2;
                    y1 = hudY + 0;
                    y2 = hudY + midHeight * 2;
                    break;
            }
        }
        if (RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, x1, y1, x2, y2) && button == 0) {
            hudX = alignment == SkyblockerConfig.Alignment.RIGHT ?
                    (int) mouseX + midWidth :
                    (int) mouseX - (midWidth / 2);
            hudY = (int) mouseY - (midHeight);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            hudX = this.width / 2;
            hudY = (int) (this.height * 0.6F);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_Q) {
            var current = SkyblockerConfig.get().general.titleContainer.alignment;
            SkyblockerConfig.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.MIDDLE;
                case MIDDLE -> SkyblockerConfig.Alignment.RIGHT;
                case RIGHT -> SkyblockerConfig.Alignment.LEFT;
            };
        }
        if(keyCode == GLFW.GLFW_KEY_E) {
            var current = SkyblockerConfig.get().general.titleContainer.alignment;
            SkyblockerConfig.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.RIGHT;
                case MIDDLE -> SkyblockerConfig.Alignment.LEFT;
                case RIGHT -> SkyblockerConfig.Alignment.MIDDLE;
            };
        }
        if(keyCode == GLFW.GLFW_KEY_R) {
            var current = SkyblockerConfig.get().general.titleContainer.direction;
            SkyblockerConfig.get().general.titleContainer.direction = switch (current) {
                case HORIZONTAL -> SkyblockerConfig.Direction.VERTICAL;
                case VERTICAL -> SkyblockerConfig.Direction.HORIZONTAL;
            };
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        SkyblockerConfig.get().general.titleContainer.x = hudX;
        SkyblockerConfig.get().general.titleContainer.y = hudY;
        AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        super.close();
    }
}
