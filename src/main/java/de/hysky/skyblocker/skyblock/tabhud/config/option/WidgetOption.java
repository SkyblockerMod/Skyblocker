package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

public interface WidgetOption<T> {

	T getValue();

	void setValue(T value);

	String getId();

	JsonElement toJson();

	void fromJson(JsonElement json);

	/**
	 * {@link Widget#setX(int)}, {@link Widget#setY(int)} and {@link ClickableWidget#setWidth(int)} should be properly implemented.
	 * Height will not be changed and kept as is.
	 * @return a new widget instance
	 */
	ClickableWidget createNewWidget(WidgetConfig config);

}
