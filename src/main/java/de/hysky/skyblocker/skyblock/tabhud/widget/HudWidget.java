package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.config.option.FloatOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.PositionRuleOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public abstract class HudWidget extends AbstractWidget {
	private float scale = 1.0f;

	public static String nameToId(String name) {
		return name.toLowerCase(Locale.ENGLISH).replace(' ', '_').replace("'", "");
	}

	private PositionRule positionRule;
	protected abstract void renderWidget(DrawContext context, float delta);

	protected abstract void renderWidgetConfig(DrawContext context, float delta);

	public abstract @NotNull Information getInformation();

	public boolean shouldRender() {
		return true;
	}

	public final void render(DrawContext context, float delta) {
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(scale, scale);
		renderWidget(context, delta);
		matrices.popMatrix();
	}

	public final void render(DrawContext context) {
		render(context, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	public final void renderConfig(DrawContext context, float delta) {
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		matrices.scale(scale, scale);
		renderWidgetConfig(context, delta);
		matrices.popMatrix();
	}

	public final void renderConfig(DrawContext context) {
		renderConfig(context, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderWidget(context, delta);
	}

	/**
	 * Add options to the list that will be saved and show up in the config screen. These options should be per widget, not global.
	 * @param options list to add to.
	 * @apiNote REMEMBER TO CALL SUPER
	 */
	public void getPerScreenOptions(List<WidgetOption<?>> options) {
		options.add(new FloatOption("scale", Text.literal("Scale"), this::getScale, this::setScale, 1f).setMinAndMax(0.2f, 2f));
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

	@Override
	public ScreenRect getNavigationFocus() {
		return new ScreenRect(getX(), getY(), getScaledWidth(), getScaledHeight());
	}

	/**
	 * @param id the id for the config file
	 * @param displayName the name that will be shown in the config screen
	 * @param available in which locations the widget can be added. If not available everywhere, {@link java.util.EnumSet} and {@code contains} are recommended
	 */
	public record Information(String id, Text displayName, Predicate<Location> available) {
		/**
		 * Shorter constructor that makes the widget available everywhere
		 * @see Information#Information(String, Text, Predicate)
		 */
		public Information(String id, Text displayName) {
			this(id, displayName, (location) -> true);
		}
	}

	private boolean positioned = false;
	private boolean visible = false;
	/**
	 * Used for the config screen.
	 */
	private boolean inherited = false;


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

	public final boolean isInherited() {
		return inherited;
	}

	public final void setInherited(boolean inherited) {
		this.inherited = inherited;
	}

	@Override
	public final boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getX() && mouseX <= getX() + getScaledWidth() && mouseY >= getY() && mouseY < getY() + getScaledHeight();
	}
}
