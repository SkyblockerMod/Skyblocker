package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/// This widget shows various dungeon info:
/// deaths, healing, damage taken, and milestones.
@RegisterWidget
public class DungeonDeathWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Death").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

	// match the deaths entry
	// group 1: amount of deaths
	private static final Pattern DEATH_PATTERN = Pattern.compile("Team Deaths: (?<deathnum>\\d+).*");

	public DungeonDeathWidget() {
		super("Dungeon Deaths", TITLE, TextColor.RED.getValue());
	}

	@Override
	public void updateContent(List<Component> ignored) {
		Matcher m = PlayerListManager.regexAt(25, DEATH_PATTERN);
		if (m == null) {
			this.addComponent(Elements.iconTextComponent());
		} else {
			ChatFormatting f = m.group("deathnum").equals("0") ? ChatFormatting.GREEN : ChatFormatting.RED;
			this.addSimpleIcoText(Ico.SKULL, "Deaths: ", f, m.group("deathnum"));
		}

		this.addSimpleIcoText(Ico.IRON_SWORD, "Damage Dealt:", ChatFormatting.RED, 26);
		this.addSimpleIcoText(Ico.POTION, "Healing Done:", ChatFormatting.RED, 27);
		this.addSimpleIcoText(Ico.NTAG, "Milestone:", ChatFormatting.YELLOW, 28);
	}
}
