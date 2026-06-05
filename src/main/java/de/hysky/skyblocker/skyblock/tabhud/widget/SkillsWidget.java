package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about a skill and some stats,
// as seen in the rightmost column of the default HUD
@RegisterWidget
public class SkillsWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Skill Info").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	// match the skill entry
	// group 1: skill name and level
	// group 2: progress to next level (without "%")
	private static final Pattern SKILL_PATTERN = Pattern.compile("([A-Za-z]* [0-9]*): ([0-9.MAX]*)%?");

	public SkillsWidget() {
		super("Skills", TITLE, ChatFormatting.YELLOW.getColor());

	}

	@Override
	public void updateContent(List<Component> lines) {
		for (Component line : lines) {
			Element progress;
			Matcher m = SKILL_PATTERN.matcher(line.getString());
			if (m.matches()) {
				String skill = m.group(1);
				String pcntStr = m.group(2);

				if (!pcntStr.equals("MAX")) {
					float pcnt = Float.parseFloat(pcntStr);
					progress = Elements.progressComponent(Ico.LANTERN, Component.nullToEmpty(skill), pcnt, ChatFormatting.GOLD.getColor());
				} else {
					addSimpleIcoText(Ico.LANTERN, skill + ": ", ChatFormatting.RED, pcntStr);
					continue;
				}
			} else {
				progress = new PlainTextElement(line);
			}
			this.addComponent(progress);
		}
	}
}
