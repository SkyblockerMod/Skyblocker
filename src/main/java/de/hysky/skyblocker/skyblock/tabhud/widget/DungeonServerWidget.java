package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows broad info about the current dungeon
// opened/completed rooms, % of secrets found and time taken
@RegisterWidget
public class DungeonServerWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Dungeon Info").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);

	// match the secrets text
	// group 1: % of secrets found (without "%")
	private static final Pattern SECRET_PATTERN = Pattern.compile("Secrets Found: (?<secnum>.*)%");

	public DungeonServerWidget() {
		super("Dungeon Info", TITLE, Formatting.DARK_PURPLE.getColorValue());
	}

	@Override
	public void updateContent(List<Text> ignored) {
		this.addSimpleIcoText(Ico.NTAG, "Name:", Formatting.AQUA, 41);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Visited:", Formatting.DARK_PURPLE, 42);
		this.addSimpleIcoText(Ico.SIGN, "Rooms Completed:", Formatting.LIGHT_PURPLE, 43);

		Matcher m = PlayerListManager.regexAt(44, SECRET_PATTERN);
		if (m == null) {
			this.addComponent(Components.progressComponent());
		} else {
			this.addComponent(Components.progressComponent(Ico.CHEST, Text.of("Secrets found:"),
					Float.parseFloat(m.group("secnum")),
					Formatting.DARK_PURPLE.getColorValue()));
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GOLD, 45);
	}
}
