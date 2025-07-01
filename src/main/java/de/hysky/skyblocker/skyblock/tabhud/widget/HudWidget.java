package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.config.option.FloatOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class HudWidget extends AbstractWidget {

	private float scale = 1.0f;

	private PositionRule positionRule;
	protected abstract void renderWidget(DrawContext context, float delta);

	public abstract void renderConfig(DrawContext context, float delta);

	public abstract Information getInformation();

	public boolean shouldRender() {
		return true;
	}

	public final void render(DrawContext context) {
		renderWidget(context, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	public final void renderConfig(DrawContext context) {
		renderConfig(context, MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks());
	}

	@Override
	public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderWidget(context, delta);
	}

	public List<WidgetOption<?>> getOptions() {
		List<WidgetOption<?>> options = new ArrayList<>();
		options.add(new FloatOption("scale", Text.literal("Scale"), this::getScale, this::setScale).setMinAndMax(0.2f, 2f));
		return options;
	}

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

	/**
	 * @param id the id for the config file
	 * @param displayName the name that will be shown in the config screen
	 * @param available in which locations the widget can be added.
	 */
	public record Information(String id, Text displayName, Predicate<Location> available) {}

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
