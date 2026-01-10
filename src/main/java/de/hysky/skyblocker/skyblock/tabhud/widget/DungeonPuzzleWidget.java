package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
		super("Dungeon Puzzles", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
	}

	@Override
	public void updateContent() {
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
			this.addComponent(Components.iconTextComponent(Ico.SIGN, t));
			pos++;
		}
		if (pos == 48) {
			this.addComponent(Components.iconTextComponent(Ico.BARRIER, Component.literal("No puzzles!").withStyle(ChatFormatting.GRAY)));
		}
	}

	@Override
	protected List<de.hysky.skyblocker.skyblock.tabhud.widget.component.Component> getConfigComponents() {
		return List.of(new PlainTextComponent(Component.literal("Puzzles")));
	}

	@Override
	protected void updateContent(List<Component> lines) {}
}
