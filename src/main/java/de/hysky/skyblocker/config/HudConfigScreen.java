package de.hysky.skyblocker.config;

import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.List;

/**
 * A screen for configuring the positions of HUD widgets.
 * <p>
 * Note: This is currently only used for title container. There is a new system for other HUD widgets, see {@link de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen}
 * <p>
 * This class takes care of rendering the widgets, dragging them, and resetting their positions.
 * Create one subclass for each collection of HUD widgets that are displayed at the same time.
 * (i.e. one for dwarven mines, one for the end, etc.) See an implementation for an example.
 */
public abstract class HudConfigScreen extends Screen {
	protected final Screen parent;
	protected final List<AbstractWidget> widgets;

	private AbstractWidget draggingWidget;
	private double mouseClickRelativeX;
	private double mouseClickRelativeY;

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widget
	 *
	 * @param title  the title of the screen
	 * @param parent the parent screen
	 * @param widget the widget to configure
	 */
	public HudConfigScreen(Text title, Screen parent, AbstractWidget widget) {
		this(title, parent, List.of(widget));
	}

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widgets
	 *
	 * @param title   the title of the screen
	 * @param parent  the parent screen
	 * @param widgets the widgets to configure
	 */
	public HudConfigScreen(Text title, Screen parent, List<AbstractWidget> widgets) {
		super(title);
		this.parent = parent;
		this.widgets = widgets;
	}

	@Override
	protected void init() {
		super.init();
		// Reset positions here, so width and height are available.
		resetPos();
	}

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		renderWidget(context, widgets, delta);
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
	}

	/**
	 * Renders the widgets using the default {@link AbstractWidget#render(DrawContext, int, int, float)} method. Override to change the behavior.
	 *
	 * @param context the context to render in
	 * @param widgets the widgets to render
	 */
	protected void renderWidget(DrawContext context, List<AbstractWidget> widgets, float delta) {
		for (AbstractWidget widget : widgets) {
			widget.render(context, -1, -1, delta);
		}
	}

	@Override
	public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button == 0 && draggingWidget != null) {
			draggingWidget.setX((int) Math.clamp(mouseX - mouseClickRelativeX, 0, this.width - draggingWidget.getWidth()) - getWidgetXOffset(draggingWidget));
			draggingWidget.setY((int) Math.clamp(mouseY - mouseClickRelativeY, 0, this.height - draggingWidget.getHeight()));
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public final boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (AbstractWidget widget : widgets) {
				if (HudHelper.pointIsInArea(mouseX, mouseY, widget.getX() + getWidgetXOffset(widget), widget.getY(), widget.getX() + getWidgetXOffset(widget) + widget.getWidth(), widget.getY() + widget.getHeight())) {
					draggingWidget = widget;
					mouseClickRelativeX = mouseX - widget.getX() - getWidgetXOffset(widget);
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

	protected int getWidgetXOffset(AbstractWidget widget) {
		return 0;
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
			AbstractWidget widget = widgets.get(i);
			IntIntMutablePair configPos = configPositions.get(i);
			widget.setX(configPos.leftInt());
			widget.setY(configPos.rightInt());
		}
	}

	/**
	 * Returns the positions of the widgets in the config
	 *
	 * @param config the config to get the positions from
	 * @return the positions of the widgets
	 */
	protected abstract List<IntIntMutablePair> getConfigPos(SkyblockerConfig config);

	@Override
	public final void close() {
		SkyblockerConfigManager.update(config -> savePos(config, widgets));
		client.setScreen(parent);
	}

	/**
	 * Saves the passed positions to the config.
	 * <p>
	 * NOTE: The config manager will save the config right after this method is called.
	 *
	 * @param configManager the config so you don't have to get it
	 * @param widgets       the widgets to save
	 * @see SkyblockerConfigManager#update(java.util.function.Consumer)
	 */
	protected abstract void savePos(SkyblockerConfig configManager, List<AbstractWidget> widgets);
}
