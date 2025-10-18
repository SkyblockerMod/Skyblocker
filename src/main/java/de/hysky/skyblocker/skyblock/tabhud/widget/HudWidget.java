package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.Set;

public abstract class HudWidget extends AbstractWidget {
	/**
	 * Single constant set for representing all possible locations for a {@code HudWidget} to prevent unnecessarily
	 * recreating this set many times over (not the best for efficiency).
	 */
	protected static final Set<Location> ALL_LOCATIONS = Set.of(Location.values());
	private final String internalID;


	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param internalID the internal ID, for config, positioning depending on other widgets, all that good stuff
	 */
	public HudWidget(String internalID) {
		this.internalID = internalID;
	}


	/**
	 * Whether the widget should render in this location. Using this instead of rendering nothing will
	 * allow "child" widgets to take this one's spot when not rendered. <br><br>
	 * {@link de.hysky.skyblocker.utils.Utils#getLocation()} should not be used unless you know what you're doing.
	 * (might not be true anymore, need testing still c: )
	 *
	 * @param location the location
	 * @return true if the widget should render in the specified location
	 */
	public boolean shouldRender(Location location) {
		return isEnabledIn(location);
	}

	/**
	 * @return the locations where this widget can be enabled/disabled in the widgets configuration screen
	 */
	public abstract Set<Location> availableLocations();

	public abstract void setEnabledIn(Location location, boolean enabled);

	/**
	 * @param location the location
	 * @return if the widget is enabled in this location in general. If this is true, this widget will be shown
	 * as enabled in the WidgetsConfigScreen and will render in the preview tab regardless if {@link #shouldRender(Location)}
	 * is true or not.
	 */
	public abstract boolean isEnabledIn(Location location);

	/**
	 * Perform all your logic here. Or in the {@link #renderWidget(DrawContext, int, int, float)} method if you feel like it.
	 * But this will be called much less often. See usages of it.
	 *
	 * @see #shouldUpdateBeforeRendering()
	 */
	public abstract void update();

	/**
	 * Returns true if the update method should be called right before rendering.
	 *
	 * @return true if it should update
	 */
	public boolean shouldUpdateBeforeRendering() {
		return false;
	}

	protected abstract void renderWidget(DrawContext context, int mouseX, int mouseY, float delta);

	public final void render(DrawContext context) {
		render(context, -1, -1, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderWidget(context, mouseX, mouseY, delta);
	}

	/**
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

	@Override
	public int hashCode() {
		return Objects.hash(internalID);
	}

	public String getInternalID() {
		return internalID;
	}

	public Text getDisplayName() {
		return Text.of(getInternalID());
	}

	// Positioner shenanigans

	private boolean positioned = false;
	private boolean visible = false;


	public final boolean isPositioned() {
		return positioned;
	}

	public final void setPositioned(boolean positioned) {
		this.positioned = positioned;
	}

	public final boolean isVisible() {
		return visible;
	}

	public final void setVisible(boolean visible) {
		this.visible = visible;
	}
}
