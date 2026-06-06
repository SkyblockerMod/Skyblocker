package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

public class EditableScreenBuilder extends ScreenBuilder {
	public EditableScreenBuilder() {
		super();
	}

	public LayerBuilderEditor getEditor(WidgetManager.ScreenLayer layer) {
		return new LayerBuilderEditor(get(layer));
	}

	public Layer getLayer(WidgetManager.ScreenLayer layer) {
		return new Layer(get(layer), getEditor(layer));
	}

	public record Layer(LayerBuilder builder, LayerBuilderEditor editor) {
		public void update() {
			builder.update();
			builder.getRendered().forEach(w -> w.widget.onConfigChanged());
		}
	}
}
