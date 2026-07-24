package de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetPositioner;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.function.BiFunction;

public enum Positioner implements StringRepresentable {
	TOP(TopAlignedWidgetPositioner::new),
	CENTERED(CenteredWidgetPositioner::new);

	public static final Codec<Positioner> CODEC = StringRepresentable.fromEnum(Positioner::values);

	private final BiFunction<Float, Integer, WidgetPositioner> function;

	Positioner(BiFunction<Float, Integer, WidgetPositioner> widgetPositionerSupplier) {
		function = widgetPositionerSupplier;
	}

	public WidgetPositioner getNewPositioner(float maxHeight, int screenHeight) {
		return function.apply(maxHeight, screenHeight);
	}

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String toString() {
		return I18n.get("skyblocker.config.uiAndVisuals.tabHud.defaultPosition." + name());
	}
}
