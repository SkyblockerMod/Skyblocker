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

// this widget shows various dungeon info
// deaths, healing, dmg taken, milestones
@RegisterWidget
public class DungeonDeathWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Death").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);

	// match the deaths entry
	// group 1: amount of deaths
	private static final Pattern DEATH_PATTERN = Pattern.compile("Team Deaths: (?<deathnum>\\d+).*");

	public DungeonDeathWidget() {
		super("Dungeon Deaths", TITLE, Formatting.DARK_PURPLE.getColorValue());
	}

	@Override
	public void updateContent(List<Text> ignored) {
		Matcher m = PlayerListManager.regexAt(25, DEATH_PATTERN);
		if (m == null) {
			this.addComponent(Components.iconTextComponent());
		} else {
			Formatting f = (m.group("deathnum").equals("0")) ? Formatting.GREEN : Formatting.RED;
			Text d = simpleEntryText(m.group("deathnum"), "Deaths: ", f);
			this.addComponent(Components.iconTextComponent(Ico.SKULL, d));
		}

		this.addSimpleIcoText(Ico.IRON_SWORD, "Damage Dealt:", Formatting.RED, 26);
		this.addSimpleIcoText(Ico.POTION, "Healing Done:", Formatting.RED, 27);
		this.addSimpleIcoText(Ico.NTAG, "Milestone:", Formatting.YELLOW, 28);
	}
}
