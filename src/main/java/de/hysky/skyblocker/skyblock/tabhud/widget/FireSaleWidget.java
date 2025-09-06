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

// this widget shows info about fire sales when in the hub.
// or not, if there isn't one going on
@RegisterWidget
public class FireSaleWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Fire Sales").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	// matches a fire sale item
	// group 1: item name
	// group 2: # items available
	// group 3: # items available in total (1 digit + "k")
	private static final Pattern FIRE_PATTERN = Pattern.compile("(?<item>.*): (?<avail>\\d*)/(?<total>[0-9.]*)k");

	public FireSaleWidget() {
		super("Fire Sales", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		for (int i = 1; i < lines.size(); i++) {
			Text text = lines.get(i);
			Matcher m = FIRE_PATTERN.matcher(text.getString());
			if (m.matches()) {
				String avail = m.group("avail");
				Text itemTxt = Text.literal(m.group("item"));
				float total = Float.parseFloat(m.group("total")) * 1000;
				Text prgressTxt = Text.literal(String.format("%s/%.0f", avail, total));
				float pcnt = (Float.parseFloat(avail) / (total)) * 100f;
				this.addComponent(Components.progressComponent(Ico.GOLD, itemTxt, prgressTxt, pcnt));
			} else if (text.getString().toLowerCase() instanceof String s && (s.contains("starts") || s.contains("starting"))) {
				this.addComponent(Components.iconTextComponent(Ico.CLOCK, text));
			} else {
				this.addComponent(new PlainTextComponent(text));
			}
		}
	}
}
