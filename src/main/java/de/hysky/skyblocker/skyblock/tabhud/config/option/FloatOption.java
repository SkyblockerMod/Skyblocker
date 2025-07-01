package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import de.hysky.skyblocker.utils.Formatters;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatOption implements WidgetOption<Float> {


	private final Supplier<Float> valueGetter;
	private final Consumer<Float> valueSetter;
	private final Text name;
	private final String id;

	private float min = 0;
	private float max = 1;

	public FloatOption(String id, Text name, Supplier<Float> valueGetter, Consumer<Float> valueSetter) {
		this.id = id;
		this.name = name;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
	}

	public FloatOption setMinAndMax(float min, float max) {
		this.min = min;
		this.max = max;
		return this;
	}

	@Override
	public @NotNull Float getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(@NotNull Float value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public @NotNull JsonElement toJson() {
		return Codec.floatRange(min, max).encodeStart(JsonOps.INSTANCE, valueGetter.get()).getOrThrow();
	}

	@Override
	public void fromJson(@NotNull JsonElement json) {
		valueSetter.accept(Codec.floatRange(min, max).decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
	}

	@Override
	public @NotNull ClickableWidget createNewWidget(WidgetConfig config) {
		return new Slider();
	}

	private class Slider extends SliderWidget {

		public Slider() {
			super(0, 0, 0, 0, name, 0);
			value = toProgress(valueGetter.get());
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(name.copy().append(": ").append(Formatters.SHORT_FLOAT_NUMBERS.format(min + value * (max - min))));
		}

		@Override
		protected void applyValue() {
			valueSetter.accept(fromProgress(value));
		}

		private double toProgress(float val) {
			return (val - min) / (max - min);
		}

		private float fromProgress(double val) {
			return min + (float) val * (max - min);
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			Float v = valueGetter.get();
			if (v != fromProgress(value)) {
				value = toProgress(v);
				updateMessage();
			}
			super.renderWidget(context, mouseX, mouseY, deltaTicks);
		}
	}
}
