package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows various dungeon info
// deaths, healing, dmg taken, milestones
@RegisterWidget
public class DungeonDeathWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Death").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	// match the deaths entry
	// group 1: amount of deaths
	private static final Pattern DEATH_PATTERN = Pattern.compile("Team Deaths: (?<deathnum>\\d+).*");

	public DungeonDeathWidget() {
		super("Dungeon Deaths", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
	}

	@Override
	public void updateContent(PlayerListManager.Widget ignored) {
		Matcher m = PlayerListManager.regexAt(25, DEATH_PATTERN);
		if (m == null) {
			this.addElement(Elements.iconTextComponent());
		} else {
			ChatFormatting f = (m.group("deathnum").equals("0")) ? ChatFormatting.GREEN : ChatFormatting.RED;
			Component d = ElementCollector.simpleEntryText(m.group("deathnum"), "Deaths: ", f);
			this.addElement(Elements.iconTextComponent(Ico.SKULL, d));
		}

		this.addSimpleIcoText(Ico.IRON_SWORD, "Damage Dealt:", ChatFormatting.RED, 26);
		this.addSimpleIcoText(Ico.POTION, "Healing Done:", ChatFormatting.RED, 27);
		this.addSimpleIcoText(Ico.NTAG, "Milestone:", ChatFormatting.YELLOW, 28);
	}
}
