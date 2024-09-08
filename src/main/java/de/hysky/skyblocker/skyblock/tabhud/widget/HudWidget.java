package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.Objects;

import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudWidget extends AbstractWidget {
    private final String internalID;



    /**
     * Most often than not this should be instantiated only once.
     * @param internalID the internal ID, for config, positioning depending on other widgets, all that good stuff
     */
    public HudWidget(String internalID) {
        this.internalID = internalID;
    }


	/**
	 * Whether the widget should render in this location. This should check any config if need be.
	 * This method is used in the WidgetsConfigScreen, hence the location parameter.
	 * {@link de.hysky.skyblocker.utils.Utils#getLocation()} should not be used unless you know what you're doing.
	 * @param location the location
	 * @return true if the widget should render in the specified location
	 */
    public abstract boolean shouldRender(Location location);

	/**
	 * Perform all your logic here. Or in the {@link #renderWidget(DrawContext, int, int, float)} method if you feel like it.
	 * But this will be called much less often. See usages of it.
	 * @see #shouldUpdateBeforeRendering()
	 */
    public abstract void update();

	/**
	 * Returns true if the update method should be called right before rendering.
	 * @return true if it should update
	 */
	protected boolean shouldUpdateBeforeRendering() {
		return false;
	}

    public void render(DrawContext context) {
        render(context, -1, -1, MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration());
    }

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (shouldUpdateBeforeRendering()) update();
		renderWidget(context, mouseX, mouseY, delta);
	}

	protected abstract void renderWidget(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     *
     * @param object the other HudWidget
     * @return true if they are the same instance or the internal id is the same.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        HudWidget widget = (HudWidget) object;
        return Objects.equals(getInternalID(), widget.getInternalID());
    }

    public String getInternalID() {
        return internalID;
    }

    public String getNiceName() {
        return getInternalID();
    }

    private boolean positioned = false;


    public boolean isPositioned() {
        return positioned;
    }

    public void setPositioned(boolean positioned) {
        this.positioned = positioned;
    }
}
