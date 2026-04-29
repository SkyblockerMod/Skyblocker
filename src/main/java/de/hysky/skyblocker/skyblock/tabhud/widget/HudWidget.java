package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenId;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class HudWidget implements LayoutElement {
	protected int w = 0, h = 0;
	protected int x = 0, y = 0;
	protected float scale = 1;
	private final Information information;


	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param information the internal ID, for config, positioning depending on other widgets, all that good stuff
	 */
	public HudWidget(Information information) {
		this.information = information;
	}


	protected abstract void extractWidgetRenderState(GuiGraphicsExtractor graphics, float delta);

	protected abstract void extractWidgetRenderStateForConfig(GuiGraphicsExtractor graphics, float delta);

	public final void extractRenderState(GuiGraphicsExtractor graphics, float delta) {
		graphics.pose().pushMatrix();
		graphics.pose().scale(scale);
		extractWidgetRenderState(graphics, delta);
		graphics.pose().popMatrix();
	}

	public final void extractRenderStateForConfig(GuiGraphicsExtractor graphics, float delta) {
		graphics.pose().pushMatrix();
		graphics.pose().scale(scale);
		extractWidgetRenderStateForConfig(graphics, delta);
		graphics.pose().popMatrix();
	}

	public boolean shouldRender() {
		return true;
	}

	public void load(JsonValueInput input) {
		scale = input.readFloatOr("scale", 1);
	}

	public void save(JsonObject output) {
		output.addProperty("scale", scale);
	}

	public void getOptionWidgets(OptionWidgetCollector collector) {
		collector.addWidget(RangedSliderWidget.builder()
				.defaultValue(scale)
				.optionFormatter(Component.literal("Scale"), Formatters.FLOAT_NUMBERS)
				.minMax(0.1, 5)
				.step(0.1)
				.callback(d -> scale = (float) d)
				.build());
	}

	/**
	 * @param object the other HudWidget
	 * @return true if they are the same instance or the internal id is the same.
	 */
	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;

		HudWidget widget = (HudWidget) object;
		return Objects.equals(getInternalID(), widget.getInternalID());
	}

	@Override
	public final int hashCode() {
		return getInternalID().hashCode();
	}

	public final String getInternalID() {
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
		return Math.round(this.w * scale);
	}

	public final int getHeight() {
		return Math.round(this.h * scale);
	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		// FIXME scaled
		return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
	}

	@Override
	public final void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {}

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

		public Information(String id, Component displayName, Location allowedLocation) {
			this(id, displayName, EnumSet.of(allowedLocation));
		}

		public Information(String id, Component displayName, Location allowedLocation, Location... allowedLocations) {
			this(id, displayName, EnumSet.of(allowedLocation, allowedLocations));
		}

	}

	public static String nameToId(String name) {
		return name.toLowerCase(Locale.ENGLISH).replace(' ', '_').replace("'", "");
	}
}
