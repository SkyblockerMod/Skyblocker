package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

// this widget shows broad info about the current dungeon
// opened/completed rooms, % of secrets found and time taken
@RegisterWidget
public class DungeonServerWidget extends TabHudWidget {

	private static final MutableComponent TITLE = net.minecraft.network.chat.Component.literal("Dungeon Info").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	// match the secrets text
	// group 1: % of secrets found (without "%")
	private static final Pattern SECRET_PATTERN = Pattern.compile("Secrets Found: (?<secnum>.*)%");

	public DungeonServerWidget() {
		super("Dungeon", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
		this.addSimpleIcoText(Ico.NTAG, "Name:", ChatFormatting.AQUA, 41);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Visited:", ChatFormatting.DARK_PURPLE, 42);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Completed:", ChatFormatting.LIGHT_PURPLE, 43);

		Matcher m = PlayerListManager.regexAt(44, SECRET_PATTERN);
		if (m == null) {
			this.addComponent(Components.progressComponent());
		} else {
			this.addComponent(Components.progressComponent(Ico.CHEST, net.minecraft.network.chat.Component.nullToEmpty("Secrets found:"),
					Float.parseFloat(m.group("secnum")),
					ChatFormatting.DARK_PURPLE.getColor()));
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", ChatFormatting.GOLD, 45);
	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				Components.iconTextComponent(Ico.NTAG, simpleEntryText("Catacombs", "Name:", ChatFormatting.AQUA)),
				Components.iconTextComponent(Ico.SIGN, simpleEntryText("N/A", "Rooms Visited:", ChatFormatting.AQUA)),
				Components.iconTextComponent(Ico.SIGN, simpleEntryText("N/A", "Rooms Completed:", ChatFormatting.AQUA)),
				Components.progressComponent(),
				Components.iconTextComponent(Ico.CLOCK, simpleEntryText("N/A", "Time:", ChatFormatting.GOLD))
				);
	}

	@Override
	protected void updateContent(List<net.minecraft.network.chat.Component> lines) {}
}
