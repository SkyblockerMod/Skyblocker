package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.option.BooleanOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

@RegisterWidget
public class CommsWidget extends TabHudWidget {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String ID = "commissions";
	private static final MutableComponent TITLE = net.minecraft.network.chat.Component.literal("Commissions").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	// match a comm
	// group 1: comm name
	// group 2: comm progress (without "%" for comms that show a percentage)
	public static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

	// options
	private boolean progressBar = true;

	public CommsWidget() {
		super("Commissions", TITLE, ChatFormatting.DARK_AQUA.getColor(), Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS, Location.GLACITE_MINESHAFTS);
	}

	@Override
	public void updateContent(List<net.minecraft.network.chat.Component> lines) {
		if (lines.isEmpty()) {
			this.addComponent(Components.iconTextComponent());
			return;
		}
		for (net.minecraft.network.chat.Component line : lines) {
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
					Components.progressComponent(Ico.BOOK, net.minecraft.network.chat.Component.nullToEmpty(name), percent) :
					Components.progressComponent(Ico.BOOK, net.minecraft.network.chat.Component.nullToEmpty(name), net.minecraft.network.chat.Component.nullToEmpty(barText), percent);
		}
		return barText == null ?
				Components.iconTextComponent(Ico.BOOK, net.minecraft.network.chat.Component.literal(name).append(": ").append(net.minecraft.network.chat.Component.literal(percent + "%").withColor(ColorUtils.percentToColor(percent)))) :
				Components.iconTextComponent(Ico.BOOK, net.minecraft.network.chat.Component.literal(name).append(": ").append(net.minecraft.network.chat.Component.literal(barText).withColor(ColorUtils.percentToColor(percent))));
	}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		// TODO translatable
		options.add(new BooleanOption("progress_bar", net.minecraft.network.chat.Component.literal("Progress Bar"), () -> progressBar, b -> progressBar = b, true));
	}
}
