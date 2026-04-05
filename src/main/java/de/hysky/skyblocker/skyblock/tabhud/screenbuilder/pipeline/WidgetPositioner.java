package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import java.util.function.Function;
import net.minecraft.client.gui.navigation.ScreenPosition;
import org.jspecify.annotations.Nullable;

public abstract class WidgetPositioner {
	protected final int screenWidth;
	protected final int screenHeight;

	public WidgetPositioner(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public abstract void positionWidget(HudWidget hudWidget);

	/**
	 * Called whenever all the widgets that need to be positioned have been fed to the positioner.
	 * Used for centering stuff and things
	 */
	public abstract void finalizePositioning();

	public static void applyRuleToWidget(HudWidget widget, int screenWidth, int screenHeight, Function<String, PositionRule> ruleProvider) {
		widget.setPositioned(true);
		PositionRule rule = ruleProvider.apply(widget.getInternalID());
		if (rule == null) return;

		int startX;
		int startY;
		if (rule.parent().equals("screen")) {
			startX = (int) (rule.parentPoint().horizontalPoint().getPercentage() * screenWidth);
			startY = (int) (rule.parentPoint().verticalPoint().getPercentage() * screenHeight);

		} else {
			HudWidget parentWidget = WidgetManager.widgetInstances.get(rule.parent());
			if (parentWidget == null) return;
			if (!parentWidget.isPositioned()) applyRuleToWidget(parentWidget, screenWidth, screenHeight, ruleProvider);

			// size 0 part 2
			if (parentWidget.isVisible()) {
				startX = parentWidget.getX() + (int) (rule.parentPoint().horizontalPoint().getPercentage() * parentWidget.getWidth());
				startY = parentWidget.getY() + (int) (rule.parentPoint().verticalPoint().getPercentage() * parentWidget.getHeight());
			} else {
				startX = parentWidget.getX();
				startY = parentWidget.getY();
			}

		}

		// Effectively make the widget size 0
		if (widget.isVisible()) {
			widget.setX(startX + rule.relativeX() - (int) (rule.thisPoint().horizontalPoint().getPercentage() * widget.getWidth()));
			widget.setY(startY + rule.relativeY() - (int) (rule.thisPoint().verticalPoint().getPercentage() * widget.getHeight()));
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
	public static @Nullable ScreenPosition getStartPosition(String parent, int screenWidth, int screenHeight, PositionRule.Point parentPoint) {
		if (parent.equals("screen")) {
			return new ScreenPosition(
					(int) (parentPoint.horizontalPoint().getPercentage() * screenWidth),
					(int) (parentPoint.verticalPoint().getPercentage() * screenHeight)
			);

		} else {
			HudWidget parentWidget = WidgetManager.widgetInstances.get(parent);
			if (parentWidget == null) return null;

			return new ScreenPosition(
					parentWidget.getX() + (int) (parentPoint.horizontalPoint().getPercentage() * parentWidget.getWidth()),
					parentWidget.getY() + (int) (parentPoint.verticalPoint().getPercentage() * parentWidget.getHeight()));

		}
	}

}
