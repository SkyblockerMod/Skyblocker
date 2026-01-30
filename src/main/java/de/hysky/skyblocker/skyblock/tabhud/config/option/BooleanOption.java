package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
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

public class BooleanOption implements WidgetOption<Boolean> {

	private final Supplier<Boolean> valueGetter;
	private final Consumer<Boolean> valueSetter;
	private final String id;
	private final Component name;
	private final boolean defaultValue;

	public BooleanOption(String id, Component name, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter, boolean defaultValue) {
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.id = id;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	@Override
	public Boolean getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(Boolean value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(valueGetter.get());
	}

	@Override
	public void fromJson(JsonElement json) {
		valueSetter.accept(json.getAsBoolean());
	}

	@Override
	public AbstractWidget createNewWidget(WidgetConfig config) {
		return new Button(config, createName());
	}

	private MutableComponent createName() {
		return valueGetter.get() ? Component.translatable("options.on.composed", name) : Component.translatable("options.off.composed", name);
	}

	private class Button extends AbstractButton {
		private final WidgetConfig config;

		private Button(WidgetConfig config, Component text) {
			super(0, 0, 0, 20, text);
			this.config = config;
		}

		@Override
		public void onPress(InputWithModifiers input) {
			valueSetter.accept(input.input() == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? defaultValue : !valueGetter.get());
			setMessage(createName());
			config.notifyWidget();
		}

		@Override
		protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			renderDefaultSprite(context);
			renderDefaultLabel(context.textRenderer());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}

		@Override
		protected boolean isValidClickButton(MouseButtonInfo input) {
			return input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || GLFW.GLFW_MOUSE_BUTTON_RIGHT == input.button();
		}
	}
}
