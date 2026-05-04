package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.JsonValueInput;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// I would like to remove the tab logic from the hud layer by making it a subclass but that's already taken by ConfigLayerBuilder
public class LayerBuilder {
	private static final Logger LOGGER = LogUtils.getLogger();

	protected LayerConfig config = LayerConfig.DUMMY;
	protected final Set<PositionedWidget> rendered = new ObjectOpenHashSet<>();
	protected final Set<HudWidget> renderedWidgets = new ObjectOpenHashSet<>();
	protected final List<PositionedWidget> widgets = new LinkedList<>();
	protected final List<PositionedWidget> tabWidgets = new LinkedList<>();

	private int positionsHash = 0;

	public void setConfig(LayerConfig config) {
		this.config = config;
	}

	public void update() {
		positionsHash = 0;
		widgets.clear();
		for (Map.Entry<String, WidgetConfig> entry : config.widgets.entrySet()) {
			if (entry.getValue().config().isEmpty()) continue;
			HudWidget hudWidget = WidgetManager.getWidgetOrPlaceholder(entry.getKey());
			try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
				hudWidget.load(new JsonValueInput(reporter, entry.getValue().config().get()));
			}
			if (entry.getValue().position().isEmpty()) continue;
			widgets.add(new PositionedWidget(hudWidget, entry.getValue().position().get()));
		}
		merge();
	}

	public void clearTab() {
		tabWidgets.clear();
		merge();
	}

	public void updateTab(Collection<String> ignoredWidgets) {
		Profiler.get().push("skyblocker:updateTabWidgetsList");
		Set<String> currentWidgets = PlayerListManager.getCurrentWidgets();
		List<PositionedWidget> newTabWidgets = new LinkedList<>();
		for (String s : currentWidgets) {
			HudWidget widget = PlayerListManager.getTabWidget(s);
			if (widget == null) continue;
			if (ignoredWidgets.contains(widget.getInternalID())) continue;
			PositionedWidget e = new PositionedWidget(widget, PositionRule.DEFAULT);
			e.fromTab = true;
			newTabWidgets.add(e);
		}
		if (newTabWidgets.equals(tabWidgets)) return;
		tabWidgets.clear();
		tabWidgets.addAll(newTabWidgets);
		/*for (HudWidget widget : tabWidgets) {
			if (widget instanceof ComponentBasedWidget componentBasedWidget && !componentBasedWidget.shouldUpdateBeforeRendering()) componentBasedWidget.update();
		}*/
		merge();
		Profiler.get().pop();
	}

	protected void merge() {
		rendered.clear();
		renderedWidgets.clear();
		rendered.addAll(widgets);
		rendered.addAll(tabWidgets);
		rendered.stream().map(w -> w.widget).forEach(renderedWidgets::add);
	}

	public boolean contains(HudWidget widget) {
		return renderedWidgets.contains(widget);
	}


	public void extractRenderStates(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, boolean config) {
		Profiler.get().push("skyblocker:renderHud");
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (PositionedWidget widget : rendered) {
			boolean shouldRender = widget.widget.shouldRender() || config;
			widget.visible = shouldRender;
			hash = hash * 31 + Boolean.hashCode(shouldRender);
			hash = hash * 31 + Integer.hashCode(widget.widget.getWidth());
			hash = hash * 31 + Integer.hashCode(widget.widget.getHeight());
		}
		if (positionsHash != hash) {
			positionsHash = hash;
			updatePositions(screenWidth, screenHeight);
		}
		Matrix3x2fStack pose = graphics.pose();
		float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
		for (PositionedWidget widget : rendered) {
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
		return rendered;
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(rendered.stream().filter(p -> !p.fromTab).toList(), screenWidth, screenHeight);
		if (!tabWidgets.isEmpty()) {
			WidgetPositioner positioner = SkyblockerConfigManager.get().uiAndVisuals.tabHud.defaultPositioning.getNewPositioner(0.9f, screenHeight);
			List<HudWidget> tabWidgets = rendered.stream().filter(p -> p.fromTab).map(p -> p.widget).toList();
			tabWidgets.forEach(positioner::positionWidget);
			Vector2i dimensions = positioner.finalizePositioning();
			int x = (screenWidth - dimensions.x) / 2;
			int y = (screenHeight - dimensions.y) / 2;
			tabWidgets.forEach(widget ->
				widget.setPosition(widget.getX() + x, widget.getY() + y)
			);
		}
	}

	public static void updatePositions(Collection<PositionedWidget> widgets, int screenWidth, int screenHeight) {
		Profiler.get().push("skyblocker:updatePositions");
		widgets.forEach(w -> w.positioned = false);
		for (PositionedWidget widget : widgets) {
			if (!widget.positioned) WidgetPositioner.applyRuleToWidget(
					widget,
					screenWidth,
					screenHeight,
					s -> widgets.stream().filter(w -> w.widget.getInternalID().equals(s)).findFirst().orElseThrow()
			);
		}
		Profiler.get().pop();
	}
}
