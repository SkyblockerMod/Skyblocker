package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;

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

		String clazz = m.group("class");
		String level = m.group("level");
		DungeonClass dungeonClass = DungeonClass.from(clazz);

		PlayerInfo playerInfo = PlayerListManager.getRaw(index);
		this.addComponent(new PlayerElement(playerInfo, removeDungeonClass(playerInfo.getTabListDisplayName()), true));

		if (level == null) {
			this.addComponent(new PlainTextElement(Component.literal("Player is dead").withStyle(ChatFormatting.RED)));
			return;
		}
		this.addComponent(Elements.iconTextComponent(
				dungeonClass.icon(),
				Component.literal("Class: ").append(Component.literal(clazz + " " + level).withColor(dungeonClass.color()))
		));
	}

	/// Removes the last pair of parentheses from the input component.
	/// This method is very strict on formatting so that
	/// if the formatting changes, this will just no-op.
	private @Nullable Component removeDungeonClass(@Nullable Component name) {
		if (name == null || !name.getSiblings().getLast().getString().equals(")")) return name;

		MutableComponent playerName = name.copy();
		for (int i = playerName.getSiblings().size() - 1; i >= 0; i--) {
			if (playerName.getSiblings().remove(i).getString().trim().equals("(")) {
				break;
			}
		}
		return playerName;
	}
}
