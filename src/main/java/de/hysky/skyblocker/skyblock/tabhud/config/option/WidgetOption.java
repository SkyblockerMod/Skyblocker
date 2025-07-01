package de.hysky.skyblocker.skyblock.tabhud.config.option;

import com.google.gson.JsonElement;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public interface WidgetOption<T> {

	@NotNull T getValue();

	void setValue(@NotNull T value);

	String getId();

	@NotNull JsonElement toJson();

	void fromJson(@NotNull JsonElement json);

	/**
	 * {@link Widget#setX(int)}, {@link Widget#setY(int)} and {@link ClickableWidget#setWidth(int)} should be properly implemented.
	 * Height will not be changed and kept as is.
	 * @return a new widget instance
	 */
	@NotNull ClickableWidget createNewWidget(WidgetConfig config);

}
