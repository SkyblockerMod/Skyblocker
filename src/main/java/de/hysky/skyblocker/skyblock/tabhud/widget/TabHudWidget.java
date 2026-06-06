package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
			if (WidgetManager.isWidgetInCurrentScreen(this)) update();
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

	protected void updateContentMissing() {
		for (Component component : createErrorMessage()) addElement(new PlainTextElement(component));
	}

	/**
	 * Updates the content from the hypixel widget's lines
	 */
	protected abstract void updateContent(PlayerListManager.Widget widget);

	public List<Component> createErrorMessage() {
		return List.of(
				Component.translatable("skyblocker.hud.missingTabWidget[0]", Component.literal(hypixelWidgetName).withStyle(ChatFormatting.YELLOW)),
				Component.translatable("skyblocker.hud.missingTabWidget[1]")
		);
	}

}
