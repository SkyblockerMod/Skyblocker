package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

public abstract class TabHudWidget extends ComponentBasedWidget {
	private final String hypixelWidgetName;

	protected boolean cacheForConfig = true;
	private final List<Component> cache = new ArrayList<>();

	public TabHudWidget(String hypixelWidgetName, net.minecraft.network.chat.Component title, int color, Information information) {
		super(title, color, information);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	public TabHudWidget(String hypixelWidgetName, MutableComponent title, int colorValue) {
		super(title, colorValue, nameToId(hypixelWidgetName));
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	private void init() {
		PlayerListManager.addHandledTabWidget(hypixelWidgetName, this);
		cache.add(Components.iconTextComponent());
		registerAutoUpdate();
	}

	protected void registerAutoUpdate() {
		PlayerListManager.registerTabListener(() -> {
			if (WidgetManager.isInCurrentScreen(this)) update();
		});
	}

	/**
	 * @see ComponentBasedWidget#ComponentBasedWidget(net.minecraft.network.chat.Component, Integer, String, Set)
	 */
	public TabHudWidget(String hypixelWidgetName, net.minecraft.network.chat.Component title, Integer color, Set<Location> availableLocations) {
		super(title, color, nameToId(hypixelWidgetName), availableLocations);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	public TabHudWidget(String hypixelWidgetName, net.minecraft.network.chat.Component title, Integer color, Location availableLocation) {
		this(hypixelWidgetName, title, color, EnumSet.of(availableLocation));
	}

	public TabHudWidget(String hypixelWidgetName, net.minecraft.network.chat.Component title, Integer color, Location first, Location... availableLocations) {
		this(hypixelWidgetName, title, color, EnumSet.of(first, availableLocations));
	}

	public TabHudWidget(String hypixelWidgetName, net.minecraft.network.chat.Component title, Integer color, Predicate<Location> availableIn) {
		super(title, color, nameToId(hypixelWidgetName), availableIn);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	@Override
	public void updateContent() {
		cache.clear();
		PlayerListManager.TabListWidget widget = PlayerListManager.getListWidget(hypixelWidgetName);
		if (widget == null) {
			updateTabWidgetAbsent();
		} else {
			List<net.minecraft.network.chat.Component> list = new ArrayList<>(widget.lines().size() + 1);
			if (!widget.detail().getString().isBlank()) list.add(widget.detail());
			list.addAll(widget.lines());
			updateContent(list, widget.playerListEntries());
		}
	}

	protected void updateTabWidgetAbsent() {
		for (net.minecraft.network.chat.Component text : PlayerListManager.createErrorMessage(hypixelWidgetName)) {
			addComponent(new PlainTextComponent(text));
		}
	}

	@Override
	public void optionsChanged() {
		super.optionsChanged();
		update();
	}

	@Override
	public void addComponent(Component c) {
		if (cacheForConfig) cache.add(c);
		super.addComponent(c);
	}

	/**
	 * Same as {@link #updateContent(List)} but only override if you need access to {@code playerListEntries}.
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 * 	 *              If the vanilla tab widget has text right after the : it will be put on the first line.
	 * @param playerListEntries the player list entries, which should match the lines. If there is text after the : the whole line is included.
	 * @see #updateContent(List)
	 */
	protected void updateContent(List<net.minecraft.network.chat.Component> lines, @Nullable List<PlayerInfo> playerListEntries) {
		updateContent(lines);
	}

	/**
	 * Updates the content from the hypixel widget's lines
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 *              If the vanilla tab widget has text right after the : it will be put on the first line.
	 */
	protected abstract void updateContent(List<net.minecraft.network.chat.Component> lines);

	/**
	 * Recommended to override this, I was just lazy adding a thing to all widgets.
	 */
	@Override
	protected List<Component> getConfigComponents() {
		return cache;
	}
}
