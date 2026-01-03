package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.utils.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

public abstract class TabHudWidget extends ComponentBasedWidget {
	private final String hypixelWidgetName;
	private final List<Component> cachedComponents = new ArrayList<>();


	public TabHudWidget(String hypixelWidgetName, MutableComponent title, Integer colorValue) {
		super(title, colorValue, hypixelWidgetName.toLowerCase(Locale.ENGLISH).replace(' ', '_').replace("'", ""));
		this.hypixelWidgetName = hypixelWidgetName;
	}

	public String getHypixelWidgetName() {
		return hypixelWidgetName;
	}

	@Override
	public net.minecraft.network.chat.Component getDisplayName() {
		return net.minecraft.network.chat.Component.literal(getHypixelWidgetName());
	}

	@Override
	public void updateContent() {
		cachedComponents.forEach(super::addComponent);
	}

	public void updateFromTab(List<net.minecraft.network.chat.Component> lines, @Nullable List<PlayerInfo> playerListEntries) {
		cachedComponents.clear();
		updateContent(lines, playerListEntries);
	}

	/**
	 * Controlled by Hypixel and {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager PlayerListManager}.
	 * {@link de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder#updateWidgetLists(boolean) ScreenBuilder#updateWidgetLists}
	 * take the widgets directly from {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager#tabWidgetsToShow PlayerListManager#tabWidgetsToShow}.
	 */
	@Override
	public final boolean shouldRender(Location location) {
		return false;
	}

	/**
	 * Controlled by Hypixel and {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager PlayerListManager}.
	 * {@link de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder#updateWidgetLists(boolean) ScreenBuilder#updateWidgetLists}
	 * take the widgets directly from {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager#tabWidgetsToShow PlayerListManager#tabWidgetsToShow}.
	 */
	@Override
	public final Set<Location> availableLocations() {
		return Set.of();
	}

	/**
	 * Controlled by Hypixel and {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager PlayerListManager}.
	 * {@link de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder#updateWidgetLists(boolean) ScreenBuilder#updateWidgetLists}
	 * take the widgets directly from {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager#tabWidgetsToShow PlayerListManager#tabWidgetsToShow}.
	 */
	@Override
	public final void setEnabledIn(Location location, boolean enabled) {}

	/**
	 * Controlled by Hypixel and {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager PlayerListManager}.
	 * {@link de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder#updateWidgetLists(boolean) ScreenBuilder#updateWidgetLists}
	 * take the widgets directly from {@link de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager#tabWidgetsToShow PlayerListManager#tabWidgetsToShow}.
	 */
	@Override
	public final boolean isEnabledIn(Location location) {
		return false;
	}

	/**
	 * Same as {@link #updateContent(List)} but only override if you need access to {@code playerListEntries}.
	 *
	 * @param playerListEntries the player list entries, which should match the lines.
	 *                          Null in dungeons.
	 * @see #updateContent(List)
	 */
	protected void updateContent(List<net.minecraft.network.chat.Component> lines, @Nullable List<PlayerInfo> playerListEntries) {
		updateContent(lines);
	}

	/**
	 * Updates the content from the hypixel widget's lines
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 *              If the vanilla tab widget has text right after the : they will be put on the first line.
	 */
	protected abstract void updateContent(List<net.minecraft.network.chat.Component> lines);

	@Override
	public final void addComponent(Component c) {
		cachedComponents.add(c);
	}

}
