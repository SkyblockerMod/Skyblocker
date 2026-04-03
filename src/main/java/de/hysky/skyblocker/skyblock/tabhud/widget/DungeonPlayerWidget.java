package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;

import java.util.List;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about a player in the current dungeon group
public class DungeonPlayerWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Player").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
	private static final List<String> MSGS = List.of("???", "PRESS A TO JOIN", "Invite a friend!", "But nobody came.", "More is better!");

	private final int player;

	// title needs to be changeable here
	public DungeonPlayerWidget(int player) {
		super("Dungeon Player " + player, TITLE, ChatFormatting.DARK_PURPLE.getColor());
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
		Matcher m = PlayerListManager.regexAt(start, DungeonPlayerManager.PLAYER_TAB_PATTERN);
		if (m == null) {
			this.addComponent(Elements.iconTextComponent());
			this.addComponent(Elements.iconTextComponent());
		} else {

			Component name = Component.literal("Name: ").append(Component.literal(m.group("name")).withStyle(ChatFormatting.YELLOW));
			this.addComponent(new PlayerElement(PlayerListManager.getRaw(start), name));

			String cl = m.group("class");
			String level = m.group("level");

			if (level == null) {
				PlainTextElement ptc = new PlainTextElement(
						Component.literal("Player is dead").withStyle(ChatFormatting.RED));
				this.addComponent(ptc);
			} else {
				DungeonClass dungeonClass = DungeonClass.from(cl);

				ChatFormatting clf = ChatFormatting.GRAY;
				FlexibleItemStack cli = dungeonClass.icon();
				if (dungeonClass != DungeonClass.UNKNOWN) {
					clf = ChatFormatting.LIGHT_PURPLE;
					cl += " " + m.group("level");
				}

				Component clazz = Component.literal("Class: ").append(Component.literal(cl).withStyle(clf));
				this.addComponent(Elements.iconTextComponent(cli, clazz));
			}
		}

		this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", ChatFormatting.GOLD, start + 1);
		this.addSimpleIcoText(Ico.POTION, "Revives:", ChatFormatting.DARK_PURPLE, start + 2);
	}
}
