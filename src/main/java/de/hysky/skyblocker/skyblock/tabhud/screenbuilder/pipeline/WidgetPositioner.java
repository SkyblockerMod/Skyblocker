package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

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

}
