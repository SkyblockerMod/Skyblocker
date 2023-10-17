package de.hysky.skyblocker.utils.render.title;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Set;

public class TitleContainerConfigScreen extends Screen {
    private final Title example1 = new Title(Text.literal("Test1").formatted(Formatting.RED));
    private final Title example2 = new Title(Text.literal("Test23").formatted(Formatting.AQUA));
    private final Title example3 = new Title(Text.literal("Testing1234").formatted(Formatting.DARK_GREEN));
    private float hudX = SkyblockerConfigManager.get().general.titleContainer.x;
    private float hudY = SkyblockerConfigManager.get().general.titleContainer.y;
    private final Screen parent;
    private boolean changedScale;
    
    protected TitleContainerConfigScreen() {
    	this(null);
    }

    public TitleContainerConfigScreen(Screen parent) {
		super(Text.of("Title Container HUD Config"));
		this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context, mouseX, mouseY, delta);
        TitleContainer.render(context, Set.of(example1, example2, example3), (int) hudX, (int) hudY, delta);
        SkyblockerConfig.Direction direction = SkyblockerConfigManager.get().general.titleContainer.direction;
        SkyblockerConfig.Alignment alignment = SkyblockerConfigManager.get().general.titleContainer.alignment;
        context.drawCenteredTextWithShadow(textRenderer, "Press Q/E to change Alignment: " + alignment, width / 2, textRenderer.fontHeight * 2, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Press R to change Direction: " + direction, width / 2, textRenderer.fontHeight * 3 + 5, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Press +/- to change Scale", width / 2, textRenderer.fontHeight * 4 + 10, Color.WHITE.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, textRenderer.fontHeight * 5 + 15, Color.GRAY.getRGB());

        Pair<Vector2f, Vector2f> boundingBox = getSelectionBoundingBox();
        int x1 = (int) boundingBox.getLeft().getX();
        int y1 = (int) boundingBox.getLeft().getY();
        int x2 = (int) boundingBox.getRight().getX();
        int y2 = (int) boundingBox.getRight().getY();

        context.drawHorizontalLine(x1, x2, y1, Color.RED.getRGB());
        context.drawHorizontalLine(x1, x2, y2, Color.RED.getRGB());
        context.drawVerticalLine(x1, y1, y2, Color.RED.getRGB());
        context.drawVerticalLine(x2, y1, y2, Color.RED.getRGB());
    }

    private Pair<Vector2f, Vector2f> getSelectionBoundingBox() {
    	SkyblockerConfig.Alignment alignment = SkyblockerConfigManager.get().general.titleContainer.alignment;

        float midWidth = getSelectionWidth() / 2F;
        float x1 = 0;
        float x2 = 0;
        float y1 = hudY;
        float y2 = hudY + getSelectionHeight();
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
        return new Pair<>(new Vector2f(x1, y1), new Vector2f(x2, y2));
    }

    private float getSelectionHeight() {
        float scale = (3F * (SkyblockerConfigManager.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfigManager.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                (textRenderer.fontHeight * scale) :
                (textRenderer.fontHeight + 10F) * 3F * scale;
    }

    private float getSelectionWidth() {
        float scale = (3F * (SkyblockerConfigManager.get().general.titleContainer.titleContainerScale / 100F));
        return SkyblockerConfigManager.get().general.titleContainer.direction == SkyblockerConfig.Direction.HORIZONTAL ?
                (textRenderer.getWidth("Test1") + 10 + textRenderer.getWidth("Test23") + 10 + textRenderer.getWidth("Testing1234")) * scale :
                textRenderer.getWidth("Testing1234") * scale;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        float midWidth = getSelectionWidth() / 2;
        float midHeight = getSelectionHeight() / 2;
        var alignment = SkyblockerConfigManager.get().general.titleContainer.alignment;

        Pair<Vector2f, Vector2f> boundingBox = getSelectionBoundingBox();
        float x1 = boundingBox.getLeft().getX();
        float y1 = boundingBox.getLeft().getY();
        float x2 = boundingBox.getRight().getX();
        float y2 = boundingBox.getRight().getY();

        if (RenderHelper.pointIsInArea(mouseX, mouseY, x1, y1, x2, y2) && button == 0) {
            hudX = switch (alignment) {
                case LEFT -> (int) mouseX - midWidth;
                case MIDDLE -> (int) mouseX;
                case RIGHT -> (int) mouseX + midWidth;
            };
            hudY = (int) (mouseY - midHeight);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            hudX = (float) this.width / 2;
            hudY = this.height * 0.6F;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_Q) {
        	SkyblockerConfig.Alignment current = SkyblockerConfigManager.get().general.titleContainer.alignment;
            SkyblockerConfigManager.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.MIDDLE;
                case MIDDLE -> SkyblockerConfig.Alignment.RIGHT;
                case RIGHT -> SkyblockerConfig.Alignment.LEFT;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_E) {
        	SkyblockerConfig.Alignment current = SkyblockerConfigManager.get().general.titleContainer.alignment;
            SkyblockerConfigManager.get().general.titleContainer.alignment = switch (current) {
                case LEFT -> SkyblockerConfig.Alignment.RIGHT;
                case MIDDLE -> SkyblockerConfig.Alignment.LEFT;
                case RIGHT -> SkyblockerConfig.Alignment.MIDDLE;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_R) {
        	SkyblockerConfig.Direction current = SkyblockerConfigManager.get().general.titleContainer.direction;
            SkyblockerConfigManager.get().general.titleContainer.direction = switch (current) {
                case HORIZONTAL -> SkyblockerConfig.Direction.VERTICAL;
                case VERTICAL -> SkyblockerConfig.Direction.HORIZONTAL;
            };
        }
        if (keyCode == GLFW.GLFW_KEY_EQUAL) {
            SkyblockerConfigManager.get().general.titleContainer.titleContainerScale += 10;
            changedScale = true;
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS) {
            SkyblockerConfigManager.get().general.titleContainer.titleContainerScale -= 10;
            changedScale = true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public void close() {
        SkyblockerConfigManager.get().general.titleContainer.x = (int) hudX;
        SkyblockerConfigManager.get().general.titleContainer.y = (int) hudY;
        
        if (parent instanceof YACLScreen yaclScreen) {
            ConfigCategory category = yaclScreen.config.categories().stream().filter(cat -> cat.name().getString().equals(I18n.translate("text.autoconfig.skyblocker.category.general"))).findFirst().orElseThrow();
            OptionGroup group = category.groups().stream().filter(grp -> grp.name().getString().equals(I18n.translate("text.autoconfig.skyblocker.option.general.titleContainer"))).findFirst().orElseThrow();
                    	
            Option<?> scaleOpt = group.options().get(0);
        	
            // Refresh the value in the config with the bound value
            if (changedScale) scaleOpt.forgetPendingValue();
        }
        
        SkyblockerConfigManager.save();
        this.client.setScreen(parent);
    }
}
