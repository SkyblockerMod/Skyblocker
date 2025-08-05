package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public abstract class TabHudWidget extends ComponentBasedWidget {
	private final String hypixelWidgetName;

	protected boolean cacheForConfig = true;
	private final List<Component> cache = new ArrayList<>();

	public TabHudWidget(String hypixelWidgetName, Text title, int color, Information information) {
		super(title, color, information);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	public TabHudWidget(String hypixelWidgetName, MutableText title, int colorValue) {
		super(title, colorValue, nameToId(hypixelWidgetName));
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	private void init() {
		PlayerListManager.addHandledTabWidget(hypixelWidgetName, this);
		cache.add(new IcoTextComponent());
		registerAutoUpdate();
	}

	protected void registerAutoUpdate() {
		PlayerListManager.registerListener(this::update);
	}

	/**
	 * @see ComponentBasedWidget#ComponentBasedWidget(Text, Integer, String, Set)
	 */
	public TabHudWidget(String hypixelWidgetName, Text title, Integer color, Set<Location> availableLocations) {
		super(title, color, nameToId(hypixelWidgetName), availableLocations);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	public TabHudWidget(String hypixelWidgetName, Text title, Integer color, Location availableLocation) {
		this(hypixelWidgetName, title, color, EnumSet.of(availableLocation));
	}

	public TabHudWidget(String hypixelWidgetName, Text title, Integer color, Location first, Location... availableLocations) {
		this(hypixelWidgetName, title, color, EnumSet.of(first, availableLocations));
	}

	public TabHudWidget(String hypixelWidgetName, Text title, Integer color, Predicate<Location> availableIn) {
		super(title, color, nameToId(hypixelWidgetName), availableIn);
		this.hypixelWidgetName = hypixelWidgetName;
		init();
	}

	@Override
	public void updateContent() {
		cache.clear();
		PlayerListManager.TabListWidget widget = PlayerListManager.getListWidget(hypixelWidgetName);
		if (widget == null) {
			for (Text text : PlayerListManager.createErrorMessage(hypixelWidgetName)) {
				addComponent(new PlainTextComponent(text));
			}
		} else {
			List<Text> list = new ArrayList<>(widget.lines().size() + 1);
			if (!widget.detail().getString().isBlank()) list.add(widget.detail());
			list.addAll(widget.lines());
			updateContent(list, widget.playerListEntries());
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
	protected void updateContent(List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		updateContent(lines);
	}

	/**
	 * Updates the content from the hypixel widget's lines
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 *              If the vanilla tab widget has text right after the : it will be put on the first line.
	 */
	protected abstract void updateContent(List<Text> lines);


	/**
	 * Recommended to override this, I was just lazy adding a thing to all widgets.
	 */
	@Override
	protected List<Component> getConfigComponents() {
		return cache;
	}
}
