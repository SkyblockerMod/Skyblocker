package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
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
	public @NotNull Boolean getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(@NotNull Boolean value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public @NotNull JsonElement toJson() {
		return new JsonPrimitive(valueGetter.get());
	}

	@Override
	public void fromJson(@NotNull JsonElement json) {
		valueSetter.accept(json.getAsBoolean());
	}

	@Override
	public @NotNull ClickableWidget createNewWidget(WidgetConfig config) {
		return new Button(config, createName());
	}

	private @NotNull MutableText createName() {
		return valueGetter.get() ? Text.translatable("options.on.composed", name) : Text.translatable("options.off.composed", name);
	}

	private class Button extends PressableWidget {

		private final WidgetConfig config;
		private int button;

		private Button(WidgetConfig config, Text text) {
			super(0, 0, 0, 20, text);
			this.config = config;
		}

		@Override
		public void onPress() {
			valueSetter.accept(button == 1 ? defaultValue : !valueGetter.get());
			setMessage(createName());
			config.notifyWidget();
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}

		@Override
		protected boolean isValidClickButton(int button) {
			this.button = button;
			return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || GLFW.GLFW_MOUSE_BUTTON_RIGHT == button;
		}
	}
}
