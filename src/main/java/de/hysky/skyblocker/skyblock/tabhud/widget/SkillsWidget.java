package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about a skill and some stats,
// as seen in the rightmost column of the default HUD
@RegisterWidget
public class SkillsWidget extends TabHudWidget {

	private static final MutableComponent TITLE = net.minecraft.network.chat.Component.literal("Skill Info").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	// match the skill entry
	// group 1: skill name and level
	// group 2: progress to next level (without "%")
	private static final Pattern SKILL_PATTERN = Pattern.compile("([A-Za-z]* [0-9]*): ([0-9.MAX]*)%?");

	public SkillsWidget() {
		super("Skills", TITLE, ChatFormatting.YELLOW.getColor());

	}

	@Override
	public void updateContent(List<net.minecraft.network.chat.Component> lines) {
		for (net.minecraft.network.chat.Component line : lines) {
			Component progress;
			Matcher m = SKILL_PATTERN.matcher(line.getString());
			if (m.matches()) {
				String skill = m.group(1);
				String pcntStr = m.group(2);

				if (!pcntStr.equals("MAX")) {
					float pcnt = Float.parseFloat(pcntStr);
					progress = Components.progressComponent(Ico.LANTERN, net.minecraft.network.chat.Component.nullToEmpty(skill), pcnt, ChatFormatting.GOLD.getColor());
				} else {
					addSimpleIcoText(Ico.LANTERN, skill + ": ", ChatFormatting.RED, pcntStr);
					continue;
				}
			} else {
				progress = new PlainTextComponent(line);
			}
			this.addComponent(progress);
		}
	}
}
