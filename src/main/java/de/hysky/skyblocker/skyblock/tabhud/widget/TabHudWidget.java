package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TabHudWidget extends ElementBasedWidget {
	private final String hypixelWidgetName;
	private final List<Element> cachedElements = new ArrayList<>();


	public TabHudWidget(String hypixelWidgetName, MutableComponent title, @Nullable Integer colorValue, Information information) {
		super(title, colorValue, information);
		this.hypixelWidgetName = hypixelWidgetName;
		registerAutoUpdate();
		PlayerListManager.addHandledTabWidget(hypixelWidgetName, this);
	}

	public TabHudWidget(String hypixelWidgetName, MutableComponent title, @Nullable Integer colorValue) {
		this(hypixelWidgetName, title, colorValue, new Information(nameToId(hypixelWidgetName), title.plainCopy()));
	}

	public TabHudWidget(String hypixelWidgetName, MutableComponent title, @Nullable Integer colorValue, Location location) {
		this(hypixelWidgetName, title, colorValue, new Information(nameToId(hypixelWidgetName), title.plainCopy(), location));
	}

	public TabHudWidget(String hypixelWidgetName, MutableComponent title, @Nullable Integer colorValue, Location location, Location... otherLocations) {
		this(hypixelWidgetName, title, colorValue, new Information(nameToId(hypixelWidgetName), title.plainCopy(), location, otherLocations));
	}

	protected void registerAutoUpdate() {
		PlayerListManager.registerTabListener(() -> {
			if (WidgetManager.isWidgetInCurrentLayer(this)) update();
		});
	}

	public String getHypixelWidgetName() {
		return hypixelWidgetName;
	}

	@Override
	public void updateContent() {
		PlayerListManager.Widget widget = PlayerListManager.getListWidget(hypixelWidgetName);
		if (widget != null) updateContent(widget);
		else updateContentMissing();
	}

	protected void updateContentMissing() {} // TODO

	/**
	 * Updates the content from the hypixel widget's lines
	 *
	 * @param lines the lines, they are formatted and trimmed, no blank lines will be present.
	 *              If the vanilla tab widget has text right after the : they will be put on the first line.
	 */
	protected abstract void updateContent(PlayerListManager.Widget widget);

}
