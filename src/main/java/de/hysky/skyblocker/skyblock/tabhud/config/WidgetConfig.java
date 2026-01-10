package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public interface WidgetConfig {

	void notifyWidget();

	/**
	 * The callback receives null if no widget was clicked.
	 * @param callback called once a widget was selected
	 * @param allowItself allow selecting the edited widget itself
	 */
	void promptSelectWidget(Consumer<@Nullable HudWidget> callback, boolean allowItself);

	void removeWidget(HudWidget widget);

	HudWidget getEditedWidget();

	int getScreenWidth();
	int getScreenHeight();

	void openPopup(Function<Screen, Screen> popupCreator);
}
