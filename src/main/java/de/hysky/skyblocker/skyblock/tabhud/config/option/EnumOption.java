package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

public class EnumOption<T extends Enum<T> & StringRepresentable> implements WidgetOption<T> {

	private final Supplier<T> valueGetter;
	private final Consumer<T> valueSetter;
	private final Component name;
	private final String id;
	private final T[] enumConstants;
	private final StringRepresentable.EnumCodec<T> codec;
	private final T defaultValue;

	public EnumOption(Class<T> enumClass, String id, Component name, Supplier<T> valueGetter, Consumer<T> valueSetter, T defaultValue) {
		this.id = id;
		this.name = name;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		T[] constants = enumClass.getEnumConstants();
		if (constants == null || constants.length == 0) {
			throw new IllegalArgumentException("Enum class must have at least one enum constant");
		}
		this.enumConstants = constants;
		codec = StringRepresentable.fromEnum(() -> enumConstants);
		this.defaultValue = defaultValue;
	}

	@Override
	public T getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(T value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public JsonElement toJson() {
		return codec.encodeStart(JsonOps.INSTANCE, valueGetter.get()).getOrThrow();
	}

	@Override
	public void fromJson(JsonElement json) {
		valueSetter.accept(codec.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
	}

	private MutableComponent createMessage() {
		return name.copy().append(": ").append(valueGetter.get().toString());
	}

	@Override
	public AbstractWidget createNewWidget(WidgetConfig config) {
		return new Button(config, createMessage());
	}

	private class Button extends AbstractButton {
		private final WidgetConfig config;

		private Button(WidgetConfig config, Component text) {
			super(0, 0, 0, 20, text);
			this.config = config;
		}

		@Override
		public void onPress(InputWithModifiers input) {
			valueSetter.accept(input.input() == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? defaultValue : (enumConstants[(ArrayUtils.indexOf(enumConstants, valueGetter.get()) + 1) % enumConstants.length]));
			setMessage(createMessage());
			config.notifyWidget();
		}

		@Override
		protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			renderDefaultSprite(context);
			renderDefaultLabel(context.textRenderer());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}

		@Override
		protected boolean isValidClickButton(MouseButtonInfo input) {
			return input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || GLFW.GLFW_MOUSE_BUTTON_RIGHT == input.button();
		}
	}
}
