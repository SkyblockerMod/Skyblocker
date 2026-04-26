package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

@RegisterWidget
public class CommsWidget extends TabHudWidget {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String ID = "commissions";
	private static final MutableComponent TITLE = Component.literal("Commissions").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	// match a comm
	// group 1: comm name
	// group 2: comm progress (without "%" for comms that show a percentage)
	public static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

	public CommsWidget() {
		super("Commissions", TITLE, ChatFormatting.DARK_AQUA.getColor(), new Information("commissions", Component.literal("Commissions"), Location.CRYSTAL_HOLLOWS, Location.DWARVEN_MINES, Location.GLACITE_MINESHAFTS));
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		if (widget.lines().isEmpty()) {
			this.addComponent(Elements.iconTextComponent());
			return;
		}
		for (Component line : widget.lines()) {
			Matcher m = COMM_PATTERN.matcher(line.getString());
			if (m.matches()) {
				Element element;

				String name = m.group("name");
				String progress = m.group("progress");

				if (progress.equals("DONE")) {
					element = Elements.progressComponent(Ico.BOOK, Component.nullToEmpty(name), Component.nullToEmpty(progress), 100f);
				} else {
					float percent;
					try {
						percent = Float.parseFloat(progress.substring(0, progress.length() - 1));
					} catch (NumberFormatException e) {
						LOGGER.error("[Skyblocker Comms Widget] Failed to parse number.", e);
						percent = 0;
					}
					element = Elements.progressComponent(Ico.BOOK, Component.nullToEmpty(name), percent);
				}
				this.addComponent(element);
			}
		}
	}
}
