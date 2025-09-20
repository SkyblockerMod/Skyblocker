package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows info about all puzzeles in the dungeon (name and status)
@RegisterWidget
public class DungeonPuzzleWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Puzzles").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);

	// match a puzzle entry
	// group 1: name
	// group 2: status
	// " ?.*" to diescard the solver's name if present
	// the teleport maze has a trailing whitespace that messes with the regex
	private static final Pattern PUZZLE_PATTERN = Pattern.compile("(?<name>.*): \\[(?<status>.*)\\] ?.*");

	public DungeonPuzzleWidget() {
		super("Puzzles", TITLE, Formatting.DARK_PURPLE.getColorValue(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
		if (!Utils.isInDungeons()) return;
		int pos = 48;

		while (pos < 60) {
			Matcher m = PlayerListManager.regexAt(pos, PUZZLE_PATTERN);
			if (m == null) {
				break;
			}

			Formatting statcol = switch (m.group("status")) {
				case "✦" -> Formatting.GOLD; // Unsolved
				case "✔" -> Formatting.GREEN; // Solved
				case "✖" -> Formatting.RED; // Failed
				default -> Formatting.WHITE; // Who knows if they'll add another puzzle state or not?
			};

			Text t = Text.literal(m.group("name") + ": ")
					.append(Text.literal("[").formatted(Formatting.GRAY))
					.append(Text.literal(m.group("status")).formatted(statcol, Formatting.BOLD))
					.append(Text.literal("]").formatted(Formatting.GRAY));
			this.addComponent(Components.iconTextComponent(Ico.SIGN, t));
			pos++;
		}
		if (pos == 48) {
			this.addComponent(Components.iconTextComponent(Ico.BARRIER, Text.literal("No puzzles!").formatted(Formatting.GRAY)));
		}
	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(new PlainTextComponent(Text.literal("Puzzles")));
	}

	@Override
	protected void updateContent(List<Text> lines) {}
}
