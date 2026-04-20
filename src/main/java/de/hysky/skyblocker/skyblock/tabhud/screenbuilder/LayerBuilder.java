package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class LayerBuilder {

	private final @Nullable IdentifiedLayerBuilder parent;
	private final LayerConfig config;
	private final Set<PositionedWidget> widgets = new ObjectOpenHashSet<>();
	private final List<PositionedWidget> hudWidgets = new LinkedList<>();
	private final List<PositionedWidget> tabWidgets = new LinkedList<>();

	private int positionsHash = 0;

	public LayerBuilder(LayerConfig config, @Nullable IdentifiedLayerBuilder parent) {
		this.parent = parent;
		this.config = config;
	}

	public void visit(Visitor visitor) {
		if (parent != null) {
			parent.builder().visit((s, w, screenId) -> visitor.visit(s, w, screenId != null ? screenId : parent.id()));
		}
		for (Map.Entry<String, WidgetConfig> entry : config.widgets.entrySet()) {
			visitor.visit(entry.getKey(), entry.getValue(), null);
		}
	}

	private LayerConfig.@Nullable FancyTab fancyTab() {
		return config.fancyTab == null && parent != null ? parent.builder().fancyTab() : config.fancyTab;
	}

	public LayerConfig getFullConfig() {
		LayerConfig parentConfig = new LayerConfig();
		visit((id, widgetConfig, _) -> parentConfig.widgets.put(id, widgetConfig));
		parentConfig.fancyTab = fancyTab();
		return parentConfig;
	}

	public Map<String, WidgetConfig.Meta> getWidgetMeta() {
		Map<String, WidgetConfig.Meta> widgets = new HashMap<>();
		visit(((id, widgetConfig, screenId) -> widgets.compute(id, (_, m) -> {
			if (m == null && widgetConfig.config().isEmpty()) return null;
			return new WidgetConfig.Meta(
					Optional.ofNullable(m).flatMap(WidgetConfig.Meta::inheritedFrom),
					Optional.ofNullable(screenId),
					widgetConfig
			);
		})));
		return widgets;
	}

	public void update() {
		hudWidgets.clear();
		for (Map.Entry<String, WidgetConfig> entry : getFullConfig().widgets.entrySet()) {
			if (entry.getValue().position().isEmpty()) continue;
			HudWidget hudWidget = WidgetManager.getWidget(entry.getKey());
			hudWidgets.add(new PositionedWidget(hudWidget, entry.getValue().position().get()));
			// TODO apply config to widget
		}
		merge();
	}

	public void updateTab() {
		LayerConfig.FancyTab fancyTab = fancyTab();
		if (fancyTab == null || !fancyTab.enabled) return;
		Profiler.get().push("skyblocker:updateTabWidgetsList");
		Set<String> currentWidgets = PlayerListManager.getCurrentWidgets();
		List<PositionedWidget> newTabWidgets = new LinkedList<>();
		for (String s : currentWidgets) {
			HudWidget widget = PlayerListManager.getTabWidget(s);
			if (widget == null) continue;
			if (fancyTab.hiddenWidgets.contains(widget.getInternalID())) continue;
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

	public void serializeConfig() {
		Map<String, WidgetConfig.Meta> metas = getWidgetMeta();
		config.widgets.clear();
		for (PositionedWidget widget : widgets) {
			WidgetConfig.Meta meta = metas.get(widget.widget.getInternalID());
			if (meta.inheritedFrom().isPresent()) continue;
			config.widgets.put(widget.widget.getInternalID(), new WidgetConfig(
					// FIXME instead of checking if it's empty maybe add a "removed" boolean to meta?
					meta.widgetConfig().config().isEmpty() ? Optional.empty() : Optional.of(new JsonObject()), // TODO get config from widget
					widget.fromTab ? Optional.empty() : Optional.of(widget.rule)
			));
		}
	}

	private void merge() {
		widgets.clear();
		widgets.addAll(hudWidgets);
		widgets.addAll(tabWidgets);
	}


	public void extractRenderStates(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, boolean config) {
		Profiler.get().push("skyblocker:renderHud");
		int hash = Integer.hashCode(screenWidth);
		hash = hash * 31 + Integer.hashCode(screenHeight);
		for (PositionedWidget widget : widgets) {
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
		for (PositionedWidget widget : widgets) {
			if (!widget.visible && !config) continue;
			pose.pushMatrix();
			pose.translate(widget.widget.getX(), widget.widget.getY());
			if (config) widget.widget.extractConfigRenderState(graphics, delta);
			else widget.widget.extractRenderState(graphics, delta);
			pose.popMatrix();
		}
		Profiler.get().pop();
	}



	public PositionedWidget add(HudWidget hudWidget) {
		config.widgets.put(hudWidget.getInternalID(), new WidgetConfig(new JsonObject(), PositionRule.DEFAULT));
		PositionedWidget positionedWidget = new PositionedWidget(hudWidget, PositionRule.DEFAULT);
		widgets.add(positionedWidget);
		return positionedWidget;
	}

	public void remove(HudWidget widget) {
		config.widgets.put(widget.getInternalID(), new WidgetConfig(Optional.empty(), Optional.empty()));
		widgets.removeIf(w -> w.widget.equals(widget));
	}

	public Collection<PositionedWidget> getWidgets() {
		return widgets;
	}

	public void updatePositions(int screenWidth, int screenHeight) {
		updatePositions(widgets.stream().filter(p -> !p.fromTab).toList(), screenWidth, screenHeight);
		LayerConfig fullConfig = getFullConfig();
		if (fullConfig.fancyTab != null && fullConfig.fancyTab.enabled) {
			WidgetPositioner positioner = fullConfig.fancyTab.positioner.getNewPositioner(0.9f, screenHeight);
			List<HudWidget> tabWidgets = widgets.stream().filter(p -> p.fromTab).map(p -> p.widget).toList();
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

	public interface Visitor {
		void visit(String id, WidgetConfig widgetConfig, @Nullable ScreenId screenId);
	}
}
