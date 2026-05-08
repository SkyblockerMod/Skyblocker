package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.LayerBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An editable layer builder for config purposes
 */
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
		for (PositionedWidget widget : tabWidgets) {
			if (config.widgets.containsKey(widget.widget.getInternalID())) continue;
			JsonObject conf = new JsonObject();
			widget.widget.save(conf);
			config.widgets.put(widget.widget.getInternalID(), new WidgetConfig(Optional.of(conf), Optional.empty()));
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
		config.widgets.remove(widget.getInternalID());
		widgets.removeIf(w -> w.widget.equals(widget));
		merge();
	}

	public void remove(PositionedWidget widget) {
		config.widgets.remove(widget.widget.getInternalID());
		widgets.remove(widget);
		merge();
	}

	public Map<String, PositionedWidget> getIdToWidget() {
		return idToWidget;
	}

	@Override
	public void update() {
		super.update();
		renderedWidgets.forEach(HudWidget::onConfigChanged);
	}

	@Override
	protected void merge() {
		super.merge();
		idToWidget = rendered.stream().collect(Collectors.toMap(w -> w.widget.getInternalID(), Function.identity()));
	}
}
