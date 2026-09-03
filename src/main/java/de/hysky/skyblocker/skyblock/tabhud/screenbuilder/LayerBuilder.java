package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.joml.Matrix3x2fStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.Profiler;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.JsonValueInput;

/**
 * "Builds" the rendered screen, positions widgets properly each frame and updates their configs when needed
 */
public class LayerBuilder {
	protected LayerConfig config = LayerConfig.DUMMY;
	protected final Set<HudWidget> renderedWidgets = new ObjectOpenHashSet<>();
	protected final List<PositionedWidget> widgets = new LinkedList<>();

	private int positionsHash = 0;

	public void setConfig(LayerConfig config) {
		this.config = config;
	}

	/**
	 * Updates the displayed widgets and their config based on the LayerConfig.
	 */
	public void update() {
		positionsHash = 0;
		widgets.clear();
		for (Map.Entry<String, WidgetConfig> entry : config.widgets().entrySet()) {
			if (entry.getValue().config().isEmpty()) continue;
			HudWidget hudWidget = WidgetManager.getWidgetOrPlaceholder(entry.getKey());
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(WidgetManager.LOGGER)) {
				hudWidget.load(new JsonValueInput(reporter, entry.getValue().config().get()));
			}
			if (entry.getValue().position().isEmpty()) continue;
			widgets.add(new PositionedWidget(hudWidget, entry.getValue().position().get()));
		}
		updateList();
	}

	protected void updateList() {
		renderedWidgets.clear();
		getRendered().stream().map(w -> w.widget).forEach(renderedWidgets::add);
	}

	public boolean contains(HudWidget widget) {
		return renderedWidgets.contains(widget);
	}


	public void extractRenderStates(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, boolean config) {
		Profiler.get().push("skyblocker:renderHud");
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (PositionedWidget widget : getRendered()) {
			boolean shouldRender = config || widget.widget.shouldRender();
			widget.visible = shouldRender;
			hash = hash * 31 + Boolean.hashCode(shouldRender);
			hash = hash * 31 + Boolean.hashCode(widget.fromTab);
			hash = hash * 31 + Integer.hashCode(widget.widget.getWidth());
			hash = hash * 31 + Integer.hashCode(widget.widget.getHeight());
		}
		if (positionsHash != hash) {
			positionsHash = hash;
			updatePositions(screenWidth, screenHeight);
		}
		Matrix3x2fStack pose = graphics.pose();
		float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
		for (PositionedWidget widget : getRendered()) {
			if (!widget.visible && !config) continue;
			pose.pushMatrix();
			pose.translate(widget.widget.getX(), widget.widget.getY());
			if (config) widget.widget.extractRenderStateForConfig(graphics, delta);
			else widget.widget.extractRenderState(graphics, delta);
			pose.popMatrix();
		}
		Profiler.get().pop();
	}

	public Collection<PositionedWidget> getRendered() {
		return widgets;
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(getRendered().stream().filter(p -> !p.fromTab).toList(), screenWidth, screenHeight);
	}

	public static void updatePositions(Collection<PositionedWidget> widgets, int screenWidth, int screenHeight) {
		Profiler.get().push("skyblocker:updatePositions");
		widgets.forEach(w -> w.positioned = false);
		for (PositionedWidget widget : widgets) {
			if (!widget.positioned) WidgetPositioner.applyRuleToWidget(
					widget,
					screenWidth,
					screenHeight,
					s -> widgets.stream().filter(w -> w.widget.getInternalID().equals(s)).findFirst().orElseGet(() -> {
						WidgetManager.LOGGER.warn("Tried to get parent '{}' but it doesn't exist in the layer. Please report this to the Skyblocker discord or github along with the hud_widgets.json config file.", s);
						PositionedWidget positionedWidget = new PositionedWidget(WidgetManager.getWidgetOrPlaceholder(s), PositionRule.DEFAULT);
						positionedWidget.visible = false;
						positionedWidget.positioned = true;
						return positionedWidget;
					})
			);
		}
		Profiler.get().pop();
	}
}
