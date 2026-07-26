package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An {@link ElementBasedWidget} specialized to replace/use information from one hypixel TAB widget. <p>
 * {@link TabHudWidget#updateContent(PlayerListManager.Widget)} is automatically called when the player list updates with the specified
 * hypixel TAB widget's contents.
 */
public abstract class TabHudWidget extends ElementBasedWidget {
	private final String hypixelWidgetName;


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
		Component tabKey = Minecraft.getInstance().options.keyPlayerList.getTranslatedKeyMessage();
		return List.of(
				Component.translatable("skyblocker.hud.missingTabWidget[0]", Component.literal(hypixelWidgetName).withStyle(ChatFormatting.YELLOW)),
				Component.translatable("skyblocker.hud.missingTabWidget[1]"),
				Component.translatable("skyblocker.hud.missingTabWidget[2]"),
				Component.translatable("skyblocker.hud.missingTabWidget[3]", tabKey, tabKey, TabHud.defaultTgl.getTranslatedKeyMessage())
		);
	}

}
