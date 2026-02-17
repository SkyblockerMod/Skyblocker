package de.hysky.skyblocker.config;

import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

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
	protected final @Nullable Screen parent;
	protected final List<AbstractWidget> widgets;

	private @Nullable AbstractWidget draggingWidget;
	private double mouseClickRelativeX;
	private double mouseClickRelativeY;

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widget
	 *
	 * @param title  the title of the screen
	 * @param parent the parent screen
	 * @param widget the widget to configure
	 */
	public HudConfigScreen(Component title, @Nullable Screen parent, AbstractWidget widget) {
		this(title, parent, List.of(widget));
	}

	/**
	 * Creates a new HudConfigScreen with the passed title, parent, and widgets
	 *
	 * @param title   the title of the screen
	 * @param parent  the parent screen
	 * @param widgets the widgets to configure
	 */
	public HudConfigScreen(Component title, @Nullable Screen parent, List<AbstractWidget> widgets) {
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
	public final void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		renderWidget(context, widgets, delta);
		context.drawCenteredString(font, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
	}

	/**
	 * Renders the widgets using the default {@link AbstractWidget#render(GuiGraphics, int, int, float)} method. Override to change the behavior.
	 *
	 * @param context the context to render in
	 * @param widgets the widgets to render
	 */
	protected void renderWidget(GuiGraphics context, List<AbstractWidget> widgets, float delta) {
		for (AbstractWidget widget : widgets) {
			widget.render(context, -1, -1, delta);
		}
	}

	@Override
	public final boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
		if (click.button() == 0 && draggingWidget != null) {
			draggingWidget.setX((int) Math.clamp(click.x() - mouseClickRelativeX, 0, this.width - draggingWidget.getWidth()) - getWidgetXOffset(draggingWidget));
			draggingWidget.setY((int) Math.clamp(click.y() - mouseClickRelativeY, 0, this.height - draggingWidget.getHeight()));
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public final boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 0) {
			for (AbstractWidget widget : widgets) {
				if (HudHelper.pointIsInArea(click.x(), click.y(), widget.getX() + getWidgetXOffset(widget), widget.getY(), widget.getX() + getWidgetXOffset(widget) + widget.getWidth(), widget.getY() + widget.getHeight())) {
					draggingWidget = widget;
					mouseClickRelativeX = click.x() - widget.getX() - getWidgetXOffset(widget);
					mouseClickRelativeY = click.y() - widget.getY();
					break;
				}
			}
		} else if (click.button() == 1) {
			resetPos();
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public final boolean mouseReleased(MouseButtonEvent click) {
		draggingWidget = null;
		return super.mouseReleased(click);
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
	public final void onClose() {
		SkyblockerConfigManager.update(config -> savePos(config, widgets));
		minecraft.setScreen(parent);
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
