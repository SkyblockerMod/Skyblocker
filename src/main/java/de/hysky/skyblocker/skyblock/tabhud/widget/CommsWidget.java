package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.option.BooleanOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
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

	// options
	private boolean progressBar = true;

	public CommsWidget() {
		super("Commissions", TITLE, Formatting.DARK_AQUA.getColorValue(), Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS, Location.GLACITE_MINESHAFT);
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
					component = getComponent(name, progress, 100f);
				} else {
					float percent;
					try {
						percent = Float.parseFloat(progress.substring(0, progress.length() - 1));
					} catch (NumberFormatException e) {
						LOGGER.error("[Skyblocker Comms Widget] Failed to parse number.", e);
						percent = 0;
					}
					component = getComponent(name, null, percent);
				}
				this.addComponent(component);
			}
		}
	}

	private Component getComponent(String name, @Nullable String barText, float percent) {
		if (progressBar) {
			return barText == null ?
					Components.progressComponent(Ico.BOOK, Text.of(name), percent) :
					Components.progressComponent(Ico.BOOK, Text.of(name), Text.of(barText), percent);
		}
		return barText == null ?
				Components.iconTextComponent(Ico.BOOK, Text.literal(name).append(": ").append(Text.literal(percent + "%").withColor(ColorUtils.percentToColor(percent)))):
				Components.iconTextComponent(Ico.BOOK, Text.literal(name).append(": ").append(Text.literal(barText).withColor(ColorUtils.percentToColor(percent))));
	}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		// TODO translatable
		options.add(new BooleanOption("progress_bar", Text.literal("Progress Bar"), () -> progressBar, b -> progressBar = b, true));
	}
}
