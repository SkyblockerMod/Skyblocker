package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import java.util.function.Function;
import net.minecraft.client.gui.navigation.ScreenPosition;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

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

	public static void applyRuleToWidget(PositionedWidget widget, int screenWidth, int screenHeight, Function<String, PositionedWidget> widgetProvider) {
		widget.positioned = true;
		PositionRule rule = widget.rule;

		int startX;
		int startY;
		if (rule.parent().isEmpty()) {
			startX = (int) (rule.parentPoint().horizontalPoint().getPercentage() * screenWidth);
			startY = (int) (rule.parentPoint().verticalPoint().getPercentage() * screenHeight);

		} else {
			PositionedWidget parentWidget = widgetProvider.apply(rule.parent().get());
			if (parentWidget.fromTab) return;
			if (!parentWidget.positioned) applyRuleToWidget(parentWidget, screenWidth, screenHeight, widgetProvider);

			// size 0 part 2
			if (parentWidget.visible) {
				startX = parentWidget.widget.getX() + (int) (rule.parentPoint().horizontalPoint().getPercentage() * parentWidget.widget.getWidth());
				startY = parentWidget.widget.getY() + (int) (rule.parentPoint().verticalPoint().getPercentage() * parentWidget.widget.getHeight());
			} else {
				startX = parentWidget.widget.getX();
				startY = parentWidget.widget.getY();
			}

		}

		// Effectively make the widget size 0
		if (widget.visible) {
			widget.widget.setX(startX + rule.relativeX() - (int) (rule.thisPoint().horizontalPoint().getPercentage() * widget.widget.getWidth()));
			widget.widget.setY(startY + rule.relativeY() - (int) (rule.thisPoint().verticalPoint().getPercentage() * widget.widget.getHeight()));
		} else {
			widget.widget.setX(startX + rule.relativeX());
			widget.widget.setY(startY + rule.relativeY());
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
	public static ScreenPosition getStartPosition(@Nullable String parent, int screenWidth, int screenHeight, PositionRule.Point parentPoint) {
		if (parent == null) {
			return new ScreenPosition(
					(int) (parentPoint.horizontalPoint().getPercentage() * screenWidth),
					(int) (parentPoint.verticalPoint().getPercentage() * screenHeight)
			);

		} else {
			HudWidget parentWidget = WidgetManager.WIDGET_INSTANCES.get(parent);
			if (parentWidget == null) return new ScreenPosition(0, 0);

			return new ScreenPosition(
					parentWidget.getX() + (int) (parentPoint.horizontalPoint().getPercentage() * parentWidget.getWidth()),
					parentWidget.getY() + (int) (parentPoint.verticalPoint().getPercentage() * parentWidget.getHeight()));

		}
	}

}
