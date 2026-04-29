package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.LayerBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigLayerBuilder extends LayerBuilder {

	private Map<String, PositionedWidget> idToWidget = Map.of();

	public void serializeConfig() {
		config.widgets.replaceAll((id, widgetConfig) -> {
			PositionedWidget widget = idToWidget.get(id);
			if (widget == null) {
				if (widgetConfig.config().isEmpty()) return widgetConfig; // normal, it was removed
				// maybe not normal? not sure how to handle it // TODO
				return widgetConfig;
			}
			JsonObject conf = new JsonObject();
			widget.widget.save(conf);
			return new WidgetConfig(Optional.of(conf), widget.fromTab ? Optional.empty() : Optional.of(widget.rule));
		});
		if (config.fancyTab != null && config.fancyTab.enabled) {
			for (PositionedWidget widget : tabWidgets) {
				if (config.widgets.containsKey(widget.widget.getInternalID())) continue;
				JsonObject conf = new JsonObject();
				widget.widget.save(conf);
				config.widgets.put(widget.widget.getInternalID(), new WidgetConfig(Optional.of(conf), Optional.empty()));
			}
		}
	}

	public PositionedWidget add(HudWidget hudWidget, PositionRule rule) {
		config.widgets.put(hudWidget.getInternalID(), new WidgetConfig(new JsonObject(), rule));
		PositionedWidget positionedWidget = new PositionedWidget(hudWidget, rule);
		widgets.add(positionedWidget);
		merge();
		return positionedWidget;
	}

	public PositionedWidget add(HudWidget hudWidget) {
		return add(hudWidget, PositionRule.DEFAULT);
	}

	public void remove(HudWidget widget) {
		config.widgets.put(widget.getInternalID(), WidgetConfig.disabled());
		widgets.removeIf(w -> w.widget.equals(widget));
		merge();
	}

	public Map<String, PositionedWidget> getIdToWidget() {
		return idToWidget;
	}

	public WidgetConfig.@Nullable Meta getMeta(String id) {
		return config.getMeta(id);
	}

	@Override
	protected void merge() {
		super.merge();
		idToWidget = rendered.stream().collect(Collectors.toMap(w -> w.widget.getInternalID(), Function.identity()));
	}
}
