package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
		super("Team Deaths", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
		Matcher m = PlayerListManager.regexAt(25, DEATH_PATTERN);
		if (m == null) {
			this.addComponent(Components.iconTextComponent());
		} else {
			ChatFormatting f = (m.group("deathnum").equals("0")) ? ChatFormatting.GREEN : ChatFormatting.RED;
			Component d = simpleEntryText(m.group("deathnum"), "Deaths: ", f);
			this.addComponent(Components.iconTextComponent(Ico.SKULL, d));
		}

		this.addSimpleIcoText(Ico.IRON_SWORD, "Damage Dealt:", ChatFormatting.RED, 26);
		this.addSimpleIcoText(Ico.POTION, "Healing Done:", ChatFormatting.RED, 27);
		this.addSimpleIcoText(Ico.NTAG, "Milestone:", ChatFormatting.YELLOW, 28);
	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				Components.iconTextComponent(Ico.SKULL, simpleEntryText("0", "Deaths:", ChatFormatting.GREEN)),
				Components.iconTextComponent(Ico.IRON_SWORD, simpleEntryText("200", "Damage Dealt:", ChatFormatting.RED)),
				Components.iconTextComponent(Ico.POTION, simpleEntryText("200", "Healing Done:", ChatFormatting.RED)),
				Components.iconTextComponent(Ico.NTAG, simpleEntryText("???", "Milestone:", ChatFormatting.YELLOW))
		);
	}

	@Override
	protected void updateContent(List<net.minecraft.network.chat.Component> lines) {}
}
