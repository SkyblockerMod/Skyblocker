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
    private final Title example3 = new Title("Testing123456", Formatting.DARK_GREEN.getColorValue());
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
        int width1 = client.textRenderer.getWidth("Press Q/E to change Alignment");
        int width2 = client.textRenderer.getWidth("Press R to change Direction");
        context.drawText(client.textRenderer, "Press Q/E to change Alignment", (width / 2) - width1 / 2, client.textRenderer.fontHeight * 2, Color.GRAY.getRGB(), true);
        context.drawText(client.textRenderer, "Press R to change Direction", (width / 2) - width2 / 2, client.textRenderer.fontHeight * 3 + 5, Color.GRAY.getRGB(), true);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, hudX - 100, hudY - 20, hudX + 110, hudY + 30) && button == 0) {
            hudX = (int) Math.max(Math.min(mouseX, this.width - 100), 110);
            hudY = (int) Math.max(Math.min(mouseY, this.height - 30), 0);
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
