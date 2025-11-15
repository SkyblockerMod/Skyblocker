package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FloatOption implements WidgetOption<Float> {


	private final Supplier<Float> valueGetter;
	private final Consumer<Float> valueSetter;
	private final Text name;
	private final String id;
	private final float defaultValue;

	private float scrollStep = 0.01f;
	private NumberFormat formatter = Util.make(NumberFormat.getNumberInstance(), format -> format.setMaximumFractionDigits(2));

	private float min = 0;
	private float max = 1;

	public FloatOption(String id, Text name, Supplier<Float> valueGetter, Consumer<Float> valueSetter, float defaultValue) {
		this.id = id;
		this.name = name;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.defaultValue = defaultValue;
	}

	public FloatOption setMinAndMax(float min, float max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public FloatOption setScrollStep(float scrollStep) {
		this.scrollStep = scrollStep;
		return this;
	}

	public FloatOption setFractionDigits(int fractionDigits) {
		formatter = Util.make(NumberFormat.getNumberInstance(), format -> format.setMaximumFractionDigits(fractionDigits));
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
		return new Slider(config);
	}

	private class Slider extends SliderWidget {

		private final WidgetConfig config;

		private Slider(WidgetConfig config) {
			super(0, 0, 0, 20, name, 0);
			this.config = config;
			value = toProgress(valueGetter.get());
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(name.copy().append(": ").append(formatter.format(min + value * (max - min))));
		}

		@Override
		protected void applyValue() {
			valueSetter.accept(fromProgress(value));
			config.notifyWidget();
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

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (isMouseOver(mouseX, mouseY)) {
				valueSetter.accept(Math.clamp((float) (valueGetter.get() + verticalAmount * scrollStep), min, max));
				config.notifyWidget();
			}
			return false;
		}

		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
			if (active && visible && click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isMouseOver(click.x(), click.y())) {
				value = toProgress(defaultValue);
				applyValue();
				updateMessage();
				return true;
			}
			return super.mouseClicked(click, doubled);
		}
	}
}
