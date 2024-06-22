package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

import java.util.function.Function;

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
            HudWidget parentWidget = ScreenMaster.widgetInstances.get(rule.parent());
            if (parentWidget == null) return;
            if (!parentWidget.isPositioned()) applyRuleToWidget(parentWidget, screenWidth, screenHeight, ruleProvider);

            startX = parentWidget.getX() + (int) (rule.parentPoint().horizontalPoint().getPercentage() * parentWidget.getWidth());
            startY = parentWidget.getY() + (int) (rule.parentPoint().verticalPoint().getPercentage() * parentWidget.getHeight());

        }

        // TODO: adapt to use getPercentage()
        final int relativeX = rule.relativeX();
        widget.setX(switch (rule.thisPoint().horizontalPoint()) {
            case LEFT -> startX + relativeX;
            case CENTER -> startX + relativeX - widget.getWidth() / 2;
            case RIGHT -> startX + relativeX - widget.getWidth();
        });

        final int relativeY = rule.relativeY();
        widget.setY(switch (rule.thisPoint().verticalPoint()) {
            case TOP -> startY + relativeY;
            case CENTER -> startY + relativeY - widget.getHeight() / 2;
            case BOTTOM -> startY + relativeY - widget.getHeight();
        });



    }

}
