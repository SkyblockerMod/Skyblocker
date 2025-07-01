package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface WidgetConfig {

	void updatePositions();

	/**
	 * The callback receives null if no widget was clicked.
	 * @param callback called once a widget was selected
	 * @param allowItself allow selecting the edited widget itself
	 */
	void promptSelectWidget(Consumer<@Nullable HudWidget> callback, boolean allowItself);

	HudWidget getEditedWidget();

	int getScreenWidth();
	int getScreenHeight();
}
