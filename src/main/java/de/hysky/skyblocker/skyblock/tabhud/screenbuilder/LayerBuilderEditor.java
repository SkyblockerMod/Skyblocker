package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;

import java.util.Optional;

/**
 * A class that allows adding and removing widgets from a {@link LayerBuilder} and serializing its config
 *
 */
public class LayerBuilderEditor {
	private final LayerBuilder layer;
	// package private so you cannot go around and just edit the layers in WidgetManager
	LayerBuilderEditor(LayerBuilder layer) {
		this.layer = layer;
	}

	public PositionedWidget add(HudWidget hudWidget, PositionRule rule) {
		layer.config.widgets.put(hudWidget.getInternalID(), new WidgetConfig(new JsonObject(), rule));
		PositionedWidget positionedWidget = new PositionedWidget(hudWidget, rule);
		layer.widgets.add(positionedWidget);
		layer.updateList();
		hudWidget.updateConfigPreview();
		return positionedWidget;
	}

	public PositionedWidget add(HudWidget hudWidget) {
		return add(hudWidget, PositionRule.DEFAULT);
	}

	public void remove(HudWidget widget) {
		layer.config.widgets.remove(widget.getInternalID());
		layer.widgets.removeIf(w -> w.widget.equals(widget));
		layer.updateList();
	}

	public void remove(PositionedWidget widget) {
		remove(widget.widget);
	}

	public void serializeConfig() {
		layer.config.widgets.replaceAll((id, widgetConfig) -> {
			PositionedWidget widget = layer.getRendered().stream().filter(w -> w.widget.getInternalID().equals(id)).findFirst().orElse(null);
			if (widget == null) {
				if (widgetConfig.config().isEmpty()) return widgetConfig; // normal, it was removed
				// maybe not normal? not sure how to handle it // TODO
				return widgetConfig;
			}
			JsonObject conf = new JsonObject();
			widget.widget.save(conf);
			return new WidgetConfig(Optional.of(conf), widget.fromTab ? Optional.empty() : Optional.of(widget.rule));
		});
		for (PositionedWidget widget : layer.getRendered()) {
			if (!widget.fromTab || layer.config.widgets.containsKey(widget.widget.getInternalID())) continue;
			JsonObject conf = new JsonObject();
			widget.widget.save(conf);
			layer.config.widgets.put(widget.widget.getInternalID(), new WidgetConfig(Optional.of(conf), Optional.empty()));
		}
	}

}
