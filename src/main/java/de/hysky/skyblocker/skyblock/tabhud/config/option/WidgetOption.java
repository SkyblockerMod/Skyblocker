package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

public interface WidgetOption<T> {

	T getValue();

	void setValue(T value);

	String getId();

	JsonElement toJson();

	void fromJson(JsonElement json);

	/**
	 * {@link LayoutElement#setX(int)}, {@link LayoutElement#setY(int)} and {@link AbstractWidget#setWidth(int)} should be properly implemented.
	 * Height will not be changed and kept as is.
	 * @return a new widget instance
	 */
	AbstractWidget createNewWidget(WidgetConfig config);

}
