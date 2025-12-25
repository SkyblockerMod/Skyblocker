package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about fire sales when in the hub.
// or not, if there isn't one going on
@RegisterWidget
public class FireSaleWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Fire Sales").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	// matches a fire sale item
	// group 1: item name
	// group 2: # items available
	// group 3: # items available in total (1 digit + "k")
	private static final Pattern FIRE_PATTERN = Pattern.compile("(?<item>.*): (?<avail>\\d*)/(?<total>[0-9.]*)k");

	public FireSaleWidget() {
		super("Fire Sales", TITLE, ChatFormatting.DARK_AQUA.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		for (int i = 1; i < lines.size(); i++) {
			Component text = lines.get(i);
			Matcher m = FIRE_PATTERN.matcher(text.getString());
			if (m.matches()) {
				String avail = m.group("avail");
				Component itemTxt = Component.literal(m.group("item"));
				float total = Float.parseFloat(m.group("total")) * 1000;
				Component prgressTxt = Component.literal(String.format("%s/%.0f", avail, total));
				float pcnt = (Float.parseFloat(avail) / (total)) * 100f;
				this.addComponent(Components.progressComponent(Ico.GOLD, itemTxt, prgressTxt, pcnt));
			} else if (text.getString().toLowerCase(Locale.ENGLISH) instanceof String s && (s.contains("starts") || s.contains("starting"))) {
				this.addComponent(Components.iconTextComponent(Ico.CLOCK, text));
			} else {
				this.addComponent(new PlainTextComponent(text));
			}
		}
	}
}
