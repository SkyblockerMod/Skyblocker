package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.utils.Formatters;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.text.NumberFormat;
import java.util.function.DoubleConsumer;

public class RangedSliderWidget extends SliderWidget {

	private final double min;
	private final double max;
	private final Double2ObjectFunction<Text> formatter;
	private final DoubleConsumer callback;
	private final double step;

	public RangedSliderWidget(int x, int y, int width, int height, double defaultValue, double min, double max, double step, Double2ObjectFunction<Text> formatter, DoubleConsumer callback) {
		super(x, y, width, height, Text.empty(), 0);
		this.min = min;
		this.max = max;
		this.step = step;
		this.formatter = formatter;
		this.callback = callback;
		setValue(defaultValue);
	}

	private double roundToNearestStep(double value) {
		return step <= 0 ? value : (Math.round(value / step) * step);
	}

	private double progressToValue(double progress) {
		return progress * (max - min) + min;
	}

	private double valueToProgress(double value) {
		return Math.clamp((value - min) / (max - min), 0, 1);
	}

	@Override
	protected void updateMessage() {
		setMessage(this.formatter.apply(getValue()));
	}

	public void setValue(double value) {
		this.value = valueToProgress(value);
		this.updateMessage();
	}

	public double getValue() {
		return roundToNearestStep(progressToValue(this.value));
	}

	@Override
	protected void applyValue() {
		callback.accept(getValue());
	}

	@Override
	public void onRelease(Click click) {
		super.onRelease(click);
		this.value = valueToProgress(getValue());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int x, y, width = ButtonWidget.DEFAULT_WIDTH, height = ButtonWidget.DEFAULT_HEIGHT;
		private double value, min = 0, max = 1, step;
		private Double2ObjectFunction<Text> formatter = d -> Text.literal(Formatters.DOUBLE_NUMBERS.format(d));
		private DoubleConsumer callback = d -> {};

		private Builder() {}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder height(int height) {
			this.height = height;
			return this;
		}

		public Builder minMax(double min, double max) {
			this.min = min;
			this.max = max;
			return this;
		}

		public Builder step(double step) {
			this.step = step;
			return this;
		}

		public Builder defaultValue(double value) {
			this.value = value;
			return this;
		}

		public Builder optionFormatter(Text optionName, NumberFormat formatter) {
			return formatter(d -> ScreenTexts.composeGenericOptionText(optionName, Text.literal(formatter.format(d))));
		}

		public Builder formatter(Double2ObjectFunction<Text> formatter) {
			this.formatter = formatter;
			return this;
		}

		public Builder callback(DoubleConsumer callback) {
			this.callback = callback;
			return this;
		}

		public RangedSliderWidget build() {
			return new RangedSliderWidget(x, y, width, height, value, min, max, step, formatter, callback);
		}


	}
}
