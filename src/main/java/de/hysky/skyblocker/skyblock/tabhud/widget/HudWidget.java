package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.config.option.FloatOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.PositionRuleOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.utils.Location;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public abstract class HudWidget implements LayoutElement {
	private float scale = 1.0f;
	private PositionRule positionRule = PositionRule.DEFAULT;
	private final Information information;
	public final RenderingInformation renderingInformation = new RenderingInformation();

	protected int w = 0, h = 0;
	protected int x = 0, y = 0;

	public HudWidget(Information information) {
		this.information = information;
	}

	public static String nameToId(String name) {
		return name.toLowerCase(Locale.ENGLISH).replace(' ', '_').replace("'", "");
	}

	/**
	 * Renders the widget (duh)
	 *
	 * @implNote The matrix stack is already translated. So the top left corner of the widget is (0,0)
	 */
	protected abstract void renderWidget(GuiGraphics context, float delta);

	/**
	 * @see HudWidget#renderWidget(GuiGraphics, float)
	 */
	protected abstract void renderWidgetConfig(GuiGraphics context, float delta);

	public Information getInformation() {
		return information;
	}

	public String getId() {
		return getInformation().id();
	}

	public boolean shouldRender() {
		return true;
	}

	public final void render(GuiGraphics context, float delta) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(scale, scale);
		renderWidget(context, delta);
		matrices.popMatrix();
	}

	public final void render(GuiGraphics context) {
		render(context, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
	}

	public final void renderConfig(GuiGraphics context, float delta) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(scale, scale);
		renderWidgetConfig(context, delta);
		matrices.popMatrix();
	}

	public final void renderConfig(GuiGraphics context) {
		renderConfig(context, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks());
	}

	/**
	 * Add options to the list that will be saved and show up in the config screen. These options should be per widget, not global.
	 *
	 * @param options list to add to.
	 * @apiNote REMEMBER TO CALL SUPER
	 */
	public void getPerScreenOptions(List<WidgetOption<?>> options) {
		options.add(new FloatOption("scale", Component.literal("Scale"), this::getScale, this::setScale, 1f).setMinAndMax(0.2f, 2f));
		options.add(new PositionRuleOption(this::getPositionRule, this::setPositionRule));
	}

	public void getOptions(List<WidgetOption<?>> options) {}

	/**
	 * Called when the config has changed while in the config screen.
	 */
	public void optionsChanged() {}

	public final PositionRule getPositionRule() {
		return positionRule;
	}

	public final void setPositionRule(PositionRule positionRule) {
		this.positionRule = positionRule;
	}

	public final float getScale() {
		return scale;
	}

	public final void setScale(float scale) {
		this.scale = scale;
	}

	public final int getScaledWidth() {
		return Math.round(getWidth() * scale);
	}

	public final int getScaledHeight() {
		return Math.round(getHeight() * scale);
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

	@Override
	public ScreenRectangle getRectangle() {
		return new ScreenRectangle(getX(), getY(), getScaledWidth(), getScaledHeight());
	}

	/**
	 * @param id          the id for the config file
	 * @param displayName the name that will be shown in the config screen
	 * @param available   in which locations the widget can be added. If not available everywhere, {@link java.util.EnumSet} and {@code contains} are recommended
	 */
	public record Information(String id, Component displayName, Predicate<Location> available) {
		/**
		 * Shorter constructor that makes the widget available everywhere
		 *
		 * @see Information#Information(String, Component, Predicate)
		 */
		public Information(String id, Component displayName) {
			this(id, displayName, (location) -> true);
		}

	}

	public final boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getX() && mouseX <= getX() + getScaledWidth() && mouseY >= getY() && mouseY < getY() + getScaledHeight();
	}

	/**
	 * Information used internally to render the screen.
	 */
	public static class RenderingInformation {
		public boolean visible;
		public boolean positioned;
		public boolean inherited;
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {}
}
