package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumOption<T extends Enum<T> & StringIdentifiable> implements WidgetOption<T> {

	private final Supplier<T> valueGetter;
	private final Consumer<T> valueSetter;
	private final Text name;
	private final String id;
	private final T[] enumConstants;
	private final StringIdentifiable.EnumCodec<T> codec;

	public EnumOption(Class<T> enumClass, String id, Text name, Supplier<T> valueGetter, Consumer<T> valueSetter) {
		this.id = id;
		this.name = name;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		T[] constants = enumClass.getEnumConstants();
		if (constants == null || constants.length == 0) {
			throw new IllegalArgumentException("Enum class must have at least one enum constant");
		}
		this.enumConstants = constants;
		codec = StringIdentifiable.createCodec(() -> enumConstants);
	}

	@Override
	public @NotNull T getValue() {
		return valueGetter.get();
	}

	@Override
	public void setValue(@NotNull T value) {
		valueSetter.accept(value);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public @NotNull JsonElement toJson() {
		return codec.encodeStart(JsonOps.INSTANCE, valueGetter.get()).getOrThrow();
	}

	@Override
	public void fromJson(@NotNull JsonElement json) {
		valueSetter.accept(codec.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst());
	}

	private MutableText createMessage() {
		return name.copy().append(": ").append(valueGetter.get().toString());
	}

	private void onPress(ButtonWidget button) {
		valueSetter.accept(enumConstants[(ArrayUtils.indexOf(enumConstants, valueGetter.get()) + 1) % enumConstants.length]);
		button.setMessage(createMessage());
	}

	@Override
	public @NotNull ClickableWidget createNewWidget(WidgetConfig config) {
		return ButtonWidget.builder(createMessage(), this::onPress).build();
	}


}
