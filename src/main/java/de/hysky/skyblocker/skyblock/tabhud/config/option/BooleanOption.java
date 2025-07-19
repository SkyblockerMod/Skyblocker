package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanOption implements WidgetOption<Boolean> {

	private final Supplier<Boolean> valueGetter;
	private final Consumer<Boolean> valueSetter;
	private final String id;
	private final Text name;

	public BooleanOption(String id, Text name, Supplier<Boolean> valueGetter, Consumer<Boolean> valueSetter) {
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.id = id;
		this.name = name;
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
		return ButtonWidget.builder(createName(), button -> {
			valueSetter.accept(!valueGetter.get());
			button.setMessage(createName());
		}).build();
	}

	private @NotNull MutableText createName() {
		return valueGetter.get() ? Text.translatable("options.on.composed", name) : Text.translatable("options.off.composed", name);
	}
}
