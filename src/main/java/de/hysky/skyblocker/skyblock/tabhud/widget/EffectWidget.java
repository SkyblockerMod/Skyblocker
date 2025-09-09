package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widgte shows, how many active effects you have.
// it also shows one of those in detail.
// the parsing is super suspect and should be replaced by some regexes sometime later
@RegisterWidget
public class EffectWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Active Effects").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);
	private static final Pattern COOKIE_PATTERN = Pattern.compile(".*\\nCookie Buff\\n(?<buff>.*)\\n");

	public EffectWidget() {
		super("Active Effects", TITLE, Formatting.DARK_PURPLE.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {

		if (lines.isEmpty())
			fetchFromFooter();
		else
			fetchFromWidget(lines);
	}

	private void fetchFromWidget(List<Text> lines) {
		String string = lines.getFirst().getString().replaceAll("[()]", "");
		addComponent(new PlainTextComponent(Text.literal(string).formatted(Formatting.YELLOW, Formatting.BOLD).append(Text.literal(" effect(s) active").formatted(Formatting.WHITE))));
		for (int i = 1; i < lines.size(); i++) {
			addComponent(new PlainTextComponent(lines.get(i)));
		}
	}

	private void fetchFromFooter() {
		String footertext = PlayerListManager.getFooter();

		if (footertext == null || !footertext.contains("Active Effects")) {
			this.addComponent(Components.iconTextComponent());
			return;

		}

		Matcher m = COOKIE_PATTERN.matcher(footertext);
		if (m.find() && m.group("buff") != null) {
			String buff = m.group("buff");
			if (buff.startsWith("Not")) {
				this.addComponent(Components.iconTextComponent(Ico.COOKIE, Text.of("Cookie: not active")));
			} else {
				Text cookie = Text.literal("Cookie: ").append(buff);
				this.addComponent(Components.iconTextComponent(Ico.COOKIE, cookie));
			}
		}

		String[] lines = footertext.split("Active Effects")[1].split("\n");
		if (lines.length < 2) {
			this.addComponent(Components.iconTextComponent());
			return;
		}

		if (lines[1].startsWith("No")) {
			Text txt = Text.literal("No effects active").formatted(Formatting.GRAY);
			this.addComponent(Components.iconTextComponent(Ico.POTION, txt));
		} else if (lines[1].contains("God")) {
			String timeleft = lines[1].split("! ")[1];
			Text godpot = Text.literal("God potion!").formatted(Formatting.RED);
			Text txttleft = Text.literal(timeleft).formatted(Formatting.LIGHT_PURPLE);
			this.addComponent(Components.iconFatTextComponent(Ico.POTION, godpot, txttleft));
		} else {
			String number = lines[1].substring("You have ".length());
			int idx = number.indexOf(' ');
			if (idx == -1 || lines.length < 4) {
				this.addComponent(Components.iconFatTextComponent());
				return;
			}
			number = number.substring(0, idx);
			Text active = Text.literal("Active Effects: ")
					.append(Text.literal(number).formatted(Formatting.YELLOW));

			this.addComponent(Components.iconFatTextComponent(Ico.POTION, active,
					Text.literal(lines[2]).formatted(Formatting.AQUA)));
		}
	}
}
