package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Pattern;

// this widget shows info about the secrets of the dungeon
@RegisterWidget
public class DungeonSecretWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Discoveries").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
	private static final Pattern DISCOVERIES = Pattern.compile("Discoveries: (\\d+)");

	public DungeonSecretWidget() {
		super("Dungeon Discoveries", TITLE, Formatting.DARK_PURPLE.getColorValue());
	}

	@Override
	public void updateContent(List<Text> ignored) {
		if (!DungeonScore.isDungeonStarted()) {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 30);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 31);
		} else if (PlayerListManager.regexAt(31, DISCOVERIES) != null) {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 32);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 33);
		} else {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", Formatting.YELLOW, 31);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", Formatting.YELLOW, 32);
		}
	}
}
