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

// this widget shows info about all puzzeles in the dungeon (name and status)
@RegisterWidget
public class DungeonPuzzleWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Puzzles").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	// match a puzzle entry
	// group 1: name
	// group 2: status
	// " ?.*" to diescard the solver's name if present
	// the teleport maze has a trailing whitespace that messes with the regex
	private static final Pattern PUZZLE_PATTERN = Pattern.compile("(?<name>.*): \\[(?<status>.*)\\] ?.*");

	public DungeonPuzzleWidget() {
		super("Dungeon Puzzles", TITLE, TextColor.DARK_PURPLE.getValue(), Location.DUNGEON);
	}

	@Override
	public void updateContent(PlayerListManager.Widget ignored) {
		int pos = 48;

		while (pos < 60) {
			Matcher m = PlayerListManager.regexAt(pos, PUZZLE_PATTERN);
			if (m == null) {
				break;
			}

			ChatFormatting statcol = switch (m.group("status")) {
				case "✦" -> ChatFormatting.GOLD; // Unsolved
				case "✔" -> ChatFormatting.GREEN; // Solved
				case "✖" -> ChatFormatting.RED; // Failed
				default -> ChatFormatting.WHITE; // Who knows if they'll add another puzzle state or not?
			};

			Component t = Component.literal(m.group("name") + ": ")
					.append(Component.literal("[").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(m.group("status")).withStyle(statcol, ChatFormatting.BOLD))
					.append(Component.literal("]").withStyle(ChatFormatting.GRAY));
			this.addElement(Elements.iconTextComponent(Ico.SIGN, t));
			pos++;
		}
		if (pos == 48) {
			this.addElement(Elements.iconTextComponent(Ico.BARRIER, Component.literal("No puzzles!").withStyle(ChatFormatting.GRAY)));
		}
	}
}
