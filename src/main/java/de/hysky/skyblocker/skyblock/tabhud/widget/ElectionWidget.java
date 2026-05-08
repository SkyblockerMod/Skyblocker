package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows the status or results of the current election
@RegisterWidget
public class ElectionWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Election Info").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);
	private static final Map<String, FlexibleItemStack> MAYOR_DATA = Map.ofEntries(
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
	private static final Component EL_OVER = Component.literal("Election: ")
			.append(Component.literal("Over!").withStyle(ChatFormatting.RED));
	// pattern matching a candidate while people are voting
	// group 1: name
	// group 2: % of votes
	private static final Pattern VOTE_PATTERN = Pattern.compile("(?<mayor>\\S*): \\|+ \\((?<pcnt>\\d*)%\\)");
	private static final ChatFormatting[] COLS = { ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.YELLOW };

	public ElectionWidget() {
		super("Election", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		List<Component> lines = widget.lines();
		String status = widget.detail().getString();

		if (status.contains("Over!")) {
			// election is over
			this.addElement(Elements.iconTextComponent(Ico.BARRIER, EL_OVER));

			for (Component line : lines) {
				this.addElement(new PlainTextElement(line));
			}

		} else {
			// election is going on
			this.addSimpleIcoText(Ico.CLOCK, "Ends in: ", ChatFormatting.GOLD, lines.getFirst().getString().trim());

			for (int i = 0; i < lines.size(); i++) {
				String string = lines.get(i).getString();
				Matcher m = VOTE_PATTERN.matcher(string);
				if (m.matches()) {
					String mayorname = m.group("mayor");
					String pcntstr = m.group("pcnt");
					float pcnt = Float.parseFloat(pcntstr);
					Component candidate = Component.literal(mayorname).withStyle(COLS[i]);
					this.addElement(Elements.progressComponent(MAYOR_DATA.get(mayorname), candidate, pcnt, COLS[i].getColor()));
				} else this.addElement(new PlainTextElement(lines.get(i)));
			}
		}
	}
}
