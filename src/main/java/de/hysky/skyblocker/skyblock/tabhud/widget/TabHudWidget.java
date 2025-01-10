package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class TabHudWidget extends ComponentBasedWidget {
	private final String hypixelWidgetName;
	private final List<Component> cachedComponents = new ArrayList<>();


	public TabHudWidget(String hypixelWidgetName, MutableText title, Integer colorValue) {
		super(title, colorValue, hypixelWidgetName.toLowerCase().replace(' ', '_').replace("'", ""));
		this.hypixelWidgetName = hypixelWidgetName;
	}

	public String getHypixelWidgetName() {
		return hypixelWidgetName;
	}

	@Override
	public Text getDisplayName() {
		return Text.literal(getHypixelWidgetName());
	}

	@Override
	public void updateContent() {
		cachedComponents.forEach(super::addComponent);
	}

	public void updateFromTab(List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		cachedComponents.clear();
		updateContent(lines, playerListEntries);
	}

	/**
	 * Controlled by hypxiel and PlayerListMgr
	 */
	@Override
	public final boolean shouldRender(Location location) {
		return false;
	}

	/**
	 * Controlled by hypxiel and PlayerListMgr
	 */
	@Override
	public final Set<Location> availableLocations() {
		return Set.of();
	}

	@Override
	public final void setEnabledIn(Location location, boolean enabled) {}

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
	protected void updateContent(List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		updateContent(lines);
	}

	/**
	 * Updates the content from the hypixel widget's lines
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 *              If the vanilla tab widget has text right after the : they will be put on the first line.
	 */
	protected abstract void updateContent(List<Text> lines);

	@Override
	public final void addComponent(Component c) {
		cachedComponents.add(c);
	}

}
