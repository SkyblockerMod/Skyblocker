package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows info about a skill and some stats,
// as seen in the rightmost column of the default HUD
@RegisterWidget
public class SkillsWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Skill Info").formatted(Formatting.YELLOW,
			Formatting.BOLD);

	// match the skill entry
	// group 1: skill name and level
	// group 2: progress to next level (without "%")
	private static final Pattern SKILL_PATTERN = Pattern.compile("([A-Za-z]* [0-9]*): ([0-9.MAX]*)%?");

	public SkillsWidget() {
		super("Skills", TITLE, Formatting.YELLOW.getColorValue());

	}

	@Override
	public void updateContent(List<Text> lines) {
		for (Text line : lines) {
			Component progress;
			Matcher m = SKILL_PATTERN.matcher(line.getString());
			if (m.matches()) {
				String skill = m.group(1);
				String pcntStr = m.group(2);

				if (!pcntStr.equals("MAX")) {
					float pcnt = Float.parseFloat(pcntStr);
					progress = Components.progressComponent(Ico.LANTERN, Text.of(skill), pcnt, Formatting.GOLD.getColorValue());
				} else {
					addSimpleIcoText(Ico.LANTERN, skill + ": ", Formatting.RED, pcntStr);
					continue;
				}
			} else {
				progress = new PlainTextComponent(line);
			}
			this.addComponent(progress);
		}
	}
}
