package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows broad info about the current dungeon
// opened/completed rooms, % of secrets found and time taken
@RegisterWidget
public class DungeonServerWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Dungeon Info").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	// match the secrets text
	// group 1: % of secrets found (without "%")
	private static final Pattern SECRET_PATTERN = Pattern.compile("Secrets Found: (?<secnum>.*)%");

	public DungeonServerWidget() {
		super("Dungeon Info", TITLE, TextColor.DARK_PURPLE.getValue(), Location.DUNGEON);
	}

	@Override
	public void updateContent(PlayerListManager.Widget ignored) {
		this.addSimpleIcoText(Ico.NTAG, "Name:", ChatFormatting.AQUA, 41);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Visited:", ChatFormatting.DARK_PURPLE, 42);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Completed:", ChatFormatting.LIGHT_PURPLE, 43);

		Matcher m = PlayerListManager.regexAt(44, SECRET_PATTERN);
		if (m == null) {
			this.addElement(Elements.progressComponent());
		} else {
			this.addElement(Elements.progressComponent(Ico.CHEST, Component.nullToEmpty("Secrets found:"),
					Float.parseFloat(m.group("secnum")),
					TextColor.DARK_PURPLE.getValue()));
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", ChatFormatting.GOLD, 45);
	}
}
