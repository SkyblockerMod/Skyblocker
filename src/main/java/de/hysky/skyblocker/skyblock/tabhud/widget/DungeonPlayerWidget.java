package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;

import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/// This widget shows info about a player in the current dungeon group.
public class DungeonPlayerWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Player").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
	private static final List<String> MSGS = List.of("???", "PRESS A TO JOIN", "Invite a friend!", "But nobody came.", "More is better!");

	private final int player;

	// title needs to be changeable here
	public DungeonPlayerWidget(int player) {
		super("Dungeon Player " + player, TITLE, TextColor.AQUA.getValue());
		this.player = player;
	}

	@Override
	public void updateContent(List<Component> ignored) {
		int start = 1 + (player - 1) * 4;

		if (PlayerListManager.strAt(start) == null) {
			int idx = player - 1;
			this.addComponent(Elements.iconTextComponent(Ico.SIGN, Component.literal(MSGS.get(idx)).withStyle(ChatFormatting.GRAY)));
			return;
		}

		addPlayerNameAndClass(start);

		this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", ChatFormatting.GOLD, start + 1);
		this.addSimpleIcoText(Ico.POTION, "Revives:", ChatFormatting.DARK_PURPLE, start + 2);
	}

	private void addPlayerNameAndClass(int index) {
		Matcher m = PlayerListManager.regexAt(index, DungeonPlayerManager.PLAYER_TAB_PATTERN);
		if (m == null) {
			this.addComponent(Elements.iconTextComponent());
			this.addComponent(Elements.iconTextComponent());
			return;
		}

		String name = m.group("name");
		String clazz = m.group("class");
		String level = m.group("level");
		DungeonClass dungeonClass = DungeonClass.from(clazz);

		this.addComponent(new PlayerElement(
				PlayerListManager.getRaw(index),
				Component.literal("Name: ").append(Component.literal(name).withStyle(ChatFormatting.YELLOW))
		));

		if (level == null) {
			this.addComponent(new PlainTextElement(Component.literal("Player is dead").withStyle(ChatFormatting.RED)));
			return;
		}
		this.addComponent(Elements.iconTextComponent(
				dungeonClass.icon(),
				Component.literal("Class: ").append(Component.literal(clazz + " " + level).withColor(dungeonClass.color()))
		));
	}
}
