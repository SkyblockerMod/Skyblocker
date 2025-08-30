package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface WidgetConfig {

	void notifyWidget();

	/**
	 * The callback receives null if no widget was clicked.
	 * @param callback called once a widget was selected
	 * @param allowItself allow selecting the edited widget itself
	 */
	void promptSelectWidget(@NotNull Consumer<@Nullable HudWidget> callback, boolean allowItself);

	void removeWidget(@NotNull HudWidget widget);

	HudWidget getEditedWidget();

	int getScreenWidth();
	int getScreenHeight();

	void openPopup(Function<Screen, Screen> popupCreator);
}
