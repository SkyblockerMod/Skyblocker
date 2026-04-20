package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenId;
import de.hysky.skyblocker.utils.Location;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

public abstract class HudWidget implements LayoutElement {
	protected int w = 0, h = 0;
	protected int x = 0, y = 0;
	private final Information information;


	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param information the internal ID, for config, positioning depending on other widgets, all that good stuff
	 */
	public HudWidget(Information information) {
		this.information = information;
	}


	public abstract void extractRenderState(GuiGraphicsExtractor graphics, float delta);
	public abstract void extractConfigRenderState(GuiGraphicsExtractor graphics, float delta);


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
		return getInternalID().hashCode();
	}

	public String getInternalID() {
		return information.id();
	}

	public final Information getInformation() {
		return information;
	}

	public final int getX() {
		return this.x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return this.y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return this.w;
	}

	public final int getHeight() {
		return this.h;
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		// FIXME scaled
		return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
	}

	public boolean shouldRender() {
		return true;
	}

	/**
	 * @param id          the id for the config file
	 * @param displayName the name that will be shown in the config screen
	 * @param available   in which locations the widget can be added. If not available everywhere, {@link java.util.EnumSet} and {@code contains} are recommended
	 */
	public record Information(String id, Component displayName, Predicate<ScreenId> available) {
		/**
		 * Shorter constructor that makes the widget available everywhere
		 *
		 * @see Information#Information(String, Component, Predicate)
		 */
		public Information(String id, Component displayName) {
			this(id, displayName, _ -> true);
		}

		public Information(String id, Component displayName, Set<Location> allowedLocations) {
			this(id, displayName, screenId -> screenId instanceof ScreenId.Loc(Location location) && allowedLocations.contains(location));
		}

	}
}
