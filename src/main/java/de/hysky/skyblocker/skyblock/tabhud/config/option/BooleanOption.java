package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanOption implements WidgetOption<Boolean> {

	private final Supplier<Boolean> valueGetter;
	private final Consumer<Boolean> valueSetter;
	private final String id;
	private final Text name;
	private final boolean defaultValue;

	public BooleanOption(String id, Text name, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter, boolean defaultValue) {
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
	public ClickableWidget createNewWidget(WidgetConfig config) {
		return new Button(config, createName());
	}

	private MutableText createName() {
		return valueGetter.get() ? Text.translatable("options.on.composed", name) : Text.translatable("options.off.composed", name);
	}

	private class Button extends PressableWidget {
		private final WidgetConfig config;

		private Button(WidgetConfig config, Text text) {
			super(0, 0, 0, 20, text);
			this.config = config;
		}

		@Override
		public void onPress(AbstractInput input) {
			valueSetter.accept(input.getKeycode() == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? defaultValue : !valueGetter.get());
			setMessage(createName());
			config.notifyWidget();
		}

		@Override
		protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			drawButton(context);
			drawLabel(context.getTextConsumer());
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}

		@Override
		protected boolean isValidClickButton(MouseInput input) {
			return input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT || GLFW.GLFW_MOUSE_BUTTON_RIGHT == input.button();
		}
	}
}
