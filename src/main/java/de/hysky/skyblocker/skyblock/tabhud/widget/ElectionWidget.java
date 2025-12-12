package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows the status or results of the current election
@RegisterWidget
public class ElectionWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Election Info").formatted(Formatting.YELLOW,
			Formatting.BOLD);
	private static final Map<String, ItemStack> MAYOR_DATA = Map.ofEntries(
			Map.entry("Aatrox", Ico.DIA_SWORD),
			Map.entry("Cole", Ico.IRON_PICKAXE),
			Map.entry("Diana", Ico.BONE),
			Map.entry("Diaz", Ico.GOLD),
			Map.entry("Finnegan", Ico.IRON_HOE),
			Map.entry("Foxy", Ico.SUGAR),
			Map.entry("Paul", Ico.COMPASS),
			Map.entry("Scorpius", Ico.GOLDEN_APPLE),
			Map.entry("Jerry", Ico.VILLAGER),
			Map.entry("Derpy", Ico.DBUSH),
			Map.entry("Marina", Ico.FISH_ROD),
			Map.entry("Aura", Ico.OMINOUS_BOTTLE)
			);
	private static final Text EL_OVER = Text.literal("Election: ")
			.append(Text.literal("Over!").formatted(Formatting.RED));
	// pattern matching a candidate while people are voting
	// group 1: name
	// group 2: % of votes
	private static final Pattern VOTE_PATTERN = Pattern.compile("(?<mayor>\\S*): \\|+ \\((?<pcnt>\\d*)%\\)");
	private static final Formatting[] COLS = { Formatting.RED, Formatting.LIGHT_PURPLE, Formatting.GREEN, Formatting.AQUA, Formatting.YELLOW };

	public ElectionWidget() {
		super("Election", TITLE, Formatting.YELLOW.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		String status = lines.getFirst().getString();
		if (status == null) {
			this.addComponent(Components.iconTextComponent());
			this.addComponent(Components.iconTextComponent());
			this.addComponent(Components.iconTextComponent());
			this.addComponent(Components.iconTextComponent());
			return;
		}

		if (status.contains("Over!")) {
			// election is over
			this.addComponent(Components.iconTextComponent(Ico.BARRIER, EL_OVER));

			for (int i = 1; i < lines.size(); i++) {
				this.addComponent(new PlainTextComponent(lines.get(i)));
			}

		} else {
			// election is going on
			this.addSimpleIcoText(Ico.CLOCK, "Ends in: ", Formatting.GOLD, lines.getFirst().getString().trim());

			for (int i = 1; i < lines.size(); i++) {
				String string = lines.get(i).getString();
				Matcher m = VOTE_PATTERN.matcher(string);
				if (m.matches()) {
					String mayorname = m.group("mayor");
					String pcntstr = m.group("pcnt");
					float pcnt = Float.parseFloat(pcntstr);
					Text candidate = Text.literal(mayorname).formatted(COLS[i - 1]);
					this.addComponent(Components.progressComponent(MAYOR_DATA.get(mayorname), candidate, pcnt, COLS[i - 1].getColorValue()));
				} else this.addComponent(new PlainTextComponent(lines.get(i)));
			}
		}
	}
}
