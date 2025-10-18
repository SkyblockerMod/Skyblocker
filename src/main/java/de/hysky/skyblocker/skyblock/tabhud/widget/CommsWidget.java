package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

@RegisterWidget
public class CommsWidget extends TabHudWidget {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String ID = "commissions";
	private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	// match a comm
	// group 1: comm name
	// group 2: comm progress (without "%" for comms that show a percentage)
	public static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

	public CommsWidget() {
		super("Commissions", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		if (lines.isEmpty()) {
			this.addComponent(Components.iconTextComponent());
			return;
		}
		for (Text line : lines) {
			Matcher m = COMM_PATTERN.matcher(line.getString());
			if (m.matches()) {
				Component component;

				String name = m.group("name");
				String progress = m.group("progress");

				if (progress.equals("DONE")) {
					component = Components.progressComponent(Ico.BOOK, Text.of(name), Text.of(progress), 100f);
				} else {
					float percent;
					try {
						percent = Float.parseFloat(progress.substring(0, progress.length() - 1));
					} catch (NumberFormatException e) {
						LOGGER.error("[Skyblocker Comms Widget] Failed to parse number.", e);
						percent = 0;
					}
					component = Components.progressComponent(Ico.BOOK, Text.of(name), percent);
				}
				this.addComponent(component);
			}
		}
	}
}
