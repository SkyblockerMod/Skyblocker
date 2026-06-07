package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

public class EditableScreenBuilder extends ScreenBuilder {
	public EditableScreenBuilder() {
		super();
	}

	public LayerBuilderEditor getEditor(WidgetManager.ScreenLayer layer) {
		return new LayerBuilderEditor(get(layer));
	}

	public EditableLayer getLayer(WidgetManager.ScreenLayer layer) {
		return new EditableLayer(get(layer), getEditor(layer));
	}

	public record EditableLayer(LayerBuilder builder, LayerBuilderEditor editor) {
		public void update() {
			builder.update();
			builder.getRendered().forEach(w -> w.widget.onConfigChanged());
		}
	}
}
