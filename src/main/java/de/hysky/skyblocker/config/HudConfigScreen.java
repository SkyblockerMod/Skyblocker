package de.hysky.skyblocker.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;

public abstract class HudConfigScreen extends Screen {
    private final Widget widget;
    private final Screen parent;

    private int hudX = 0;
    private int hudY = 0;
    public HudConfigScreen(Text title, Widget widget, Screen parent) {
        super(title);
        this.widget = widget;
        this.parent = parent;

        int[] posFromConfig = getPosFromConfig(SkyblockerConfigManager.get());
        hudX = posFromConfig[0];
        hudY = posFromConfig[1];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context, mouseX, mouseY, delta);
        renderWidget(context, hudX, hudY);
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        IntIntPair dims = getDimensions();
        if (RenderHelper.pointIsInArea(mouseX, mouseY, hudX, hudY, hudX + dims.leftInt(), hudY + dims.rightInt()) && button == 0) {
            hudX = (int) Math.max(Math.min(mouseX - (double) dims.leftInt() / 2, this.width - dims.leftInt()), 0);
            hudY = (int) Math.max(Math.min(mouseY - (double) dims.rightInt() / 2, this.height - dims.rightInt()), 0);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            IntIntPair dims = getDimensions();
            hudX = this.width / 2 - dims.leftInt();
            hudY = this.height / 2 - dims.rightInt();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract protected int[] getPosFromConfig(SkyblockerConfig config);

    protected IntIntPair getDimensions() {
        return new IntIntImmutablePair(widget.getHeight(), widget.getWidth());
    }

    @Override
    public void close() {
        SkyblockerConfig skyblockerConfig = SkyblockerConfigManager.get();
        savePos(skyblockerConfig, hudX, hudY);
        SkyblockerConfigManager.save();

        client.setScreen(parent);
    }

    /**
     * This method should save the passed position to the config
     * <p>
     * NOTE: The parent class will call {@link SkyblockerConfigManager#save()} right after this method
     * @param configManager the config so you don't have to get it
     * @param x x
     * @param y y
     */
    abstract protected void savePos(SkyblockerConfig configManager, int x, int y);

    abstract protected void renderWidget(DrawContext context, int x, int y);
}
