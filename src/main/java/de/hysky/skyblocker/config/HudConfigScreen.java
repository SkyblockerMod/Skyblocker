package de.hysky.skyblocker.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

/**
 * A screen for configuring the positions of HUD widgets.
 * <p>
 * This class takes care of rendering the widgets, dragging them, and resetting their positions.
 * Create one subclass for each collection of HUD widgets that are displayed at the same time.
 * (i.e. one for dwarven mines, one for the end, etc.) See an implementation for an example.
 */
public abstract class HudConfigScreen extends Screen {
    private final Screen parent;
    private final List<Widget> widgets;

    private Widget draggingWidget;
    private double mouseClickRelativeX;
    private double mouseClickRelativeY;

    /**
     * Creates a new HudConfigScreen with the passed title, parent, and widget
     * @param title the title of the screen
     * @param parent the parent screen
     * @param widget the widget to configure
     */
    public HudConfigScreen(Text title, Screen parent, Widget widget) {
        this(title, parent, List.of(widget));
    }

    /**
     * Creates a new HudConfigScreen with the passed title, parent, and widgets
     * @param title the title of the screen
     * @param parent the parent screen
     * @param widgets the widgets to configure
     */
    public HudConfigScreen(Text title, Screen parent, List<Widget> widgets) {
        super(title);
        this.parent = parent;
        this.widgets = widgets;
        resetPos();
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderWidget(context, widgets);
        context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
    }

    /**
     * Renders the widgets using the default {@link Widget#render(DrawContext, boolean)} method. Override to change the behavior.
     * @param context the context to render in
     * @param widgets the widgets to render
     */
    protected void renderWidget(DrawContext context, List<Widget> widgets) {
        for (Widget widget : widgets) {
            widget.render(context, SkyblockerConfigManager.get().general.tabHud.enableHudBackground);
        }
    }

    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingWidget != null) {
            draggingWidget.setX((int) Math.clamp(mouseX - mouseClickRelativeX, 0, this.width - draggingWidget.getWidth()));
            draggingWidget.setY((int) Math.clamp(mouseY - mouseClickRelativeY, 0, this.height - draggingWidget.getHeight()));
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Widget widget : widgets) {
                if (RenderHelper.pointIsInArea(mouseX, mouseY, widget.getX(), widget.getY(), widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight())) {
                    draggingWidget = widget;
                    mouseClickRelativeX = mouseX - widget.getX();
                    mouseClickRelativeY = mouseY - widget.getY();
                    break;
                }
            }
        } else if (button == 1) {
            resetPos();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingWidget = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Resets the positions of the widgets to the positions in the config. Override to change the behavior.
     */
    protected void resetPos() {
        List<IntIntMutablePair> configPositions = getConfigPos(SkyblockerConfigManager.get());
        if (configPositions.size() != widgets.size()) {
            throw new IllegalStateException("The number of positions (" + configPositions.size() + ") does not match the number of widgets (" + widgets.size() + ")");
        }
        for (int i = 0; i < widgets.size(); i++) {
            Widget widget = widgets.get(i);
            IntIntMutablePair configPos = configPositions.get(i);
            widget.setX(configPos.leftInt());
            widget.setY(configPos.rightInt());
        }
    }

    /**
     * Returns the positions of the widgets in the config
     * @param config the config to get the positions from
     * @return the positions of the widgets
     */
    protected abstract List<IntIntMutablePair> getConfigPos(SkyblockerConfig config);

    @Override
    public final void close() {
        SkyblockerConfig skyblockerConfig = SkyblockerConfigManager.get();
        savePos(skyblockerConfig, widgets);
        SkyblockerConfigManager.save();

        client.setScreen(parent);
    }

    /**
     * Saves the passed positions to the config.
     * <p>
     * NOTE: The parent class will call {@link SkyblockerConfigManager#save()} right after this method
     * @param configManager the config so you don't have to get it
     * @param widgets the widgets to save
     */
    protected abstract void savePos(SkyblockerConfig configManager, List<Widget> widgets);
}
