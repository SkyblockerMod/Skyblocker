package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.utils.Formatters;
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction;
import java.text.NumberFormat;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class RangedSliderWidget extends AbstractSliderButton {

	private final double min;
	private final double max;
	private final Double2ObjectFunction<Component> formatter;
	private final DoubleConsumer callback;
	private final double step;

	public RangedSliderWidget(int x, int y, int width, int height, double defaultValue, double min, double max, double step, Double2ObjectFunction<Component> formatter, DoubleConsumer callback) {
		super(x, y, width, height, Component.empty(), 0);
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
	public void onRelease(MouseButtonEvent click) {
		super.onRelease(click);
		this.value = valueToProgress(getValue());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int x, y, width = Button.DEFAULT_WIDTH, height = Button.DEFAULT_HEIGHT;
		private double value, min = 0, max = 1, step;
		private Double2ObjectFunction<Component> formatter = d -> Component.literal(Formatters.DOUBLE_NUMBERS.format(d));
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

		public Builder optionFormatter(Component optionName, NumberFormat formatter) {
			return optionFormatter(optionName, d -> Component.literal(formatter.format(d)));
		}

		public Builder optionFormatter(Component optionName, Double2ObjectFunction<Component> formatter) {
			return formatter(d -> CommonComponents.optionNameValue(optionName, formatter.apply(d)));
		}

		public Builder formatter(Double2ObjectFunction<Component> formatter) {
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
