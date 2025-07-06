package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows the status or results of the current election
@RegisterWidget
public class ElectionWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Election Info").formatted(Formatting.YELLOW,
			Formatting.BOLD);

	private static final HashMap<String, ItemStack> MAYOR_DATA = new HashMap<>();

	private static final Text EL_OVER = Text.literal("Election: ")
			.append(Text.literal("Over!").formatted(Formatting.RED));

	// pattern matching a candidate while people are voting
	// group 1: name
	// group 2: % of votes
	private static final Pattern VOTE_PATTERN = Pattern.compile("(?<mayor>\\S*): \\|+ \\((?<pcnt>\\d*)%\\)");

	static {
		MAYOR_DATA.put("Aatrox", Ico.DIA_SWORD);
		MAYOR_DATA.put("Cole", Ico.IRON_PICKAXE);
		MAYOR_DATA.put("Diana", Ico.BONE);
		MAYOR_DATA.put("Diaz", Ico.GOLD);
		MAYOR_DATA.put("Finnegan", Ico.IRON_HOE);
		MAYOR_DATA.put("Foxy", Ico.SUGAR);
		MAYOR_DATA.put("Paul", Ico.COMPASS);
		MAYOR_DATA.put("Scorpius", Ico.GOLDEN_APPLE);
		MAYOR_DATA.put("Jerry", Ico.VILLAGER);
		MAYOR_DATA.put("Derpy", Ico.DBUSH);
		MAYOR_DATA.put("Marina", Ico.FISH_ROD);
	}

	private static final Formatting[] COLS = {Formatting.RED, Formatting.LIGHT_PURPLE, Formatting.GREEN, Formatting.AQUA, Formatting.YELLOW};

	public ElectionWidget() {
		super("Election", TITLE, Formatting.YELLOW.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		String status = lines.getFirst().getString();
		if (status == null) {
			this.addComponent(new IcoTextComponent());
			this.addComponent(new IcoTextComponent());
			this.addComponent(new IcoTextComponent());
			this.addComponent(new IcoTextComponent());
			return;
		}

		if (status.contains("Over!")) {
			// election is over
			IcoTextComponent over = new IcoTextComponent(Ico.BARRIER, EL_OVER);
			this.addComponent(over);

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
