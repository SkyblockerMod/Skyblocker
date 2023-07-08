package me.xmrvizzy.skyblocker.utils.title;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

public class TitleContainerConfigScreen extends Screen {
    private final Title example1 = new Title(Text.literal("Test1").formatted(Formatting.RED));
    private final Title example2 = new Title(Text.literal("Test23").formatted(Formatting.AQUA));
    private final Title example3 = new Title(Text.literal("Testing1234").formatted(Formatting.DARK_GREEN));
    private int hudX = SkyblockerConfig.get().general.titleContainer.x;
    private int hudY = SkyblockerConfig.get().general.titleContainer.y;

    protected TitleContainerConfigScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context);
        TitleContainer.render(context, Set.of(example1, example2, example3), hudX, hudY, delta);
        SkyblockerConfig.Direction direction = SkyblockerConfig.get().general.titleContainer.direction;
        SkyblockerConfig.Alignment alignment = SkyblockerConfig.get().general.titleContainer.alignment;
        context.drawCenteredTextWithShadow(textRenderer, "Press Q/E to change Alignment: " + alignment, width / 2, textRenderer.fontHeight * 2, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Press R to change Direction: " + direction, width / 2, textRenderer.fontHeight * 3 + 5, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Press +/- to change Scale", width / 2, textRenderer.fontHeight * 4 + 10, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, textRenderer.fontHeight * 5 + 15, Color.GRAY.getRGB());

        Pair<Vector2i, Vector2i> boundingBox = getSelectionBoundingBox();
        int x1 = boundingBox.getLeft().x;
        int y1 = boundingBox.getLeft().y;
        int x2 = boundingBox.getRight().x;
        int y2 = boundingBox.getRight().y;

        context.drawHorizontalLine(x1, x2, y1, Color.RED.getRGB());
        context.drawHorizontalLine(x1, x2, y2, Color.RED.getRGB());
        context.drawVerticalLine(x1, y1, y2, Color.RED.getRGB());
        context.drawVerticalLine(x2, y1, y2, Color.RED.getRGB());
    }

    public Pair<Vector2i, Vector2i> getSelectionBoundingBox() {
        SkyblockerConfig.Alignment alignment = SkyblockerConfig.get().general.titleContainer.alignment;

        int midWidth = getSelectionWidth() / 2;
        int x1 = 0;
        int x2 = 0;
        int y1 = hudY;
        int y2 = hudY + getSelectionHeight();
        switch (alignment) {
            case RIGHT -> {
                x1 = hudX - midWidth * 2;
                x2 = hudX;
            }
            case MIDDLE -> {
                x1 = hudX - midWidth;
                x2 = hudX + midWidth;
            }
            case LEFT -> {
                x1 = hudX;
                x2 = hudX + midWidth * 2;
            }
        }
        return new Pair<>(new Vector2i(x1, y1), new Vector2i(x2, y2));
    }

    public int getSelectionHeight() {
        int scale = (int) (3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfig.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                textRenderer.fontHeight * scale :
                (textRenderer.fontHeight + 10) * 3 * scale;
    }

    public int getSelectionWidth() {
        int scale = (int) (3F * (SkyblockerConfig.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfig.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                (textRenderer.getWidth("Test1") + 10 + textRenderer.getWidth("Test23") + 10 + textRenderer.getWidth("Testing1234")) * scale :
                textRenderer.getWidth("Testing1234") * scale;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int midWidth = getSelectionWidth() / 2;
        int midHeight = getSelectionHeight() / 2;
        var alignment = SkyblockerConfig.get().general.titleContainer.alignment;

        Pair<Vector2i, Vector2i> boundingBox = getSelectionBoundingBox();
        int x1 = boundingBox.getLeft().x;
        int y1 = boundingBox.getLeft().y;
        int x2 = boundingBox.getRight().x;
        int y2 = boundingBox.getRight().y;

        if (RenderUtils.pointExistsInArea((int) mouseX, (int) mouseY, x1, y1, x2, y2) && button == 0) {
            hudX = switch (alignment) {
                case LEFT ->(int) mouseX - midWidth;
                case MIDDLE -> (int) mouseX;
                case RIGHT -> (int) mouseX + midWidth;
            };
            hudY = (int) mouseY - (midHeight);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            hudX = this.width / 2;
            hudY = this.height / 2;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_Q) {
            SkyblockerConfig.Alignment current = SkyblockerConfig.get().general.titleContainer.alignment;
            SkyblockerConfig.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.MIDDLE;
                case MIDDLE -> SkyblockerConfig.Alignment.RIGHT;
                case RIGHT -> SkyblockerConfig.Alignment.LEFT;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_E) {
            SkyblockerConfig.Alignment current = SkyblockerConfig.get().general.titleContainer.alignment;
            SkyblockerConfig.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.RIGHT;
                case MIDDLE -> SkyblockerConfig.Alignment.LEFT;
                case RIGHT -> SkyblockerConfig.Alignment.MIDDLE;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_R) {
            SkyblockerConfig.Direction current = SkyblockerConfig.get().general.titleContainer.direction;
            SkyblockerConfig.get().general.titleContainer.direction = switch (current) {
                case HORIZONTAL -> SkyblockerConfig.Direction.VERTICAL;
                case VERTICAL -> SkyblockerConfig.Direction.HORIZONTAL;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_EQUAL) {
            SkyblockerConfig.get().general.titleContainer.titleContainerScale += 10;
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS) {
            SkyblockerConfig.get().general.titleContainer.titleContainerScale -= 10;
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
