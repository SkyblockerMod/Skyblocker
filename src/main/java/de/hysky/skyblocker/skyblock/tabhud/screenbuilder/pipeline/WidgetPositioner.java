package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.gui.ScreenPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class WidgetPositioner {
	protected final float maxHeight;
	protected final int screenHeight;

	public WidgetPositioner(float maxHeight, int screenHeight) {
		this.maxHeight = maxHeight;
		this.screenHeight = screenHeight;
	}

	public abstract void positionWidget(HudWidget hudWidget);

	/**
	 * Called whenever all the widgets that need to be positioned have been fed to the positioner.
	 * Used for centering stuff and things
	 */
	public abstract Vector2i finalizePositioning();

	public static void applyRuleToWidget(HudWidget widget, int screenWidth, int screenHeight) {
		widget.setPositioned(true);
		PositionRule rule = widget.getPositionRule();
		if (rule == null) return;

		int startX;
		int startY;
		if (rule.parent().equals("screen")) {
			startX = (int) (rule.parentPoint().horizontalPoint().getPercentage() * screenWidth);
			startY = (int) (rule.parentPoint().verticalPoint().getPercentage() * screenHeight);

		} else {
			HudWidget parentWidget = WidgetManager.WIDGET_INSTANCES.get(rule.parent());
			if (parentWidget == null) return;
			if (!parentWidget.isPositioned()) applyRuleToWidget(parentWidget, screenWidth, screenHeight);

			// size 0 part 2
			if (parentWidget.isVisible()) {
				startX = parentWidget.getX() + (int) (rule.parentPoint().horizontalPoint().getPercentage() * parentWidget.getScaledWidth());
				startY = parentWidget.getY() + (int) (rule.parentPoint().verticalPoint().getPercentage() * parentWidget.getScaledHeight());
			} else {
				startX = parentWidget.getX();
				startY = parentWidget.getY();
			}

		}

		// Effectively make the widget size 0
		if (widget.isVisible()) {
			widget.setX(startX + rule.relativeX() - (int) (rule.thisPoint().horizontalPoint().getPercentage() * widget.getScaledWidth()));
			widget.setY(startY + rule.relativeY() - (int) (rule.thisPoint().verticalPoint().getPercentage() * widget.getScaledHeight()));
		} else {
			widget.setX(startX + rule.relativeX());
			widget.setY(startY + rule.relativeY());
		}
	}

	/**
	 * Returns the start position (aka the starting point of {@code relativeX} ane {@code relativeY}) of a widget based on
	 * the parent widget's position and size
	 *
	 * @param parent       The parent widget's internal ID
	 * @param screenWidth  The width of the screen
	 * @param screenHeight The height of the screen
	 * @param parentPoint  The point on the parent widget that the child widget should be positioned relative to
	 * @return The start position of the child widget
	 */
	public static @NotNull ScreenPos getStartPosition(String parent, int screenWidth, int screenHeight, PositionRule.Point parentPoint) {
		if (parent.equals("screen")) {
			return new ScreenPos(
					(int) (parentPoint.horizontalPoint().getPercentage() * screenWidth),
					(int) (parentPoint.verticalPoint().getPercentage() * screenHeight)
			);

		} else {
			HudWidget parentWidget = WidgetManager.getWidgetOrPlaceholder(parent);

			return new ScreenPos(
					parentWidget.getX() + (int) (parentPoint.horizontalPoint().getPercentage() * parentWidget.getScaledWidth()),
					parentWidget.getY() + (int) (parentPoint.verticalPoint().getPercentage() * parentWidget.getScaledHeight()));

		}
	}

	/**
	 * Returns the start position (aka the starting point of {@code relativeX} ane {@code relativeY}) of the widget based on
	 * the parent widget's position and size
	 *
	 * @param widget       The widget
	 * @param screenWidth  The width of the screen
	 * @param screenHeight The height of the screen
	 * @return The start position of the widget
	 */
	public static @NotNull ScreenPos getStartPosition(HudWidget widget, int screenWidth, int screenHeight) {
		return getStartPosition(widget.getPositionRule().parent(), screenWidth, screenHeight, widget.getPositionRule().parentPoint());
	}

}
