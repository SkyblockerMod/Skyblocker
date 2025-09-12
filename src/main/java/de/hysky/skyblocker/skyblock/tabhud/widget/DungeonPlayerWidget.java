package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.dungeon.DungeonClass;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.regex.Matcher;

// this widget shows info about a player in the current dungeon group
public class DungeonPlayerWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Player").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
	private static final List<String> MSGS = List.of("???", "PRESS A TO JOIN", "Invite a friend!", "But nobody came.", "More is better!");

	private final int player;

	// title needs to be changeable here
	public DungeonPlayerWidget(int player) {
		super("Dungeon Player " + player, TITLE, Formatting.DARK_PURPLE.getColorValue());
		this.player = player;
	}

	@Override
	public void updateContent(List<Text> ignored) {
		int start = 1 + (player - 1) * 4;

		if (PlayerListManager.strAt(start) == null) {
			int idx = player - 1;
			this.addComponent(Components.iconTextComponent(Ico.SIGN, Text.literal(MSGS.get(idx)).formatted(Formatting.GRAY)));
			return;
		}
		Matcher m = PlayerListManager.regexAt(start, DungeonPlayerManager.PLAYER_TAB_PATTERN);
		if (m == null) {
			this.addComponent(Components.iconTextComponent());
			this.addComponent(Components.iconTextComponent());
		} else {

			Text name = Text.literal("Name: ").append(Text.literal(m.group("name")).formatted(Formatting.YELLOW));
			this.addComponent(new PlayerComponent(PlayerListManager.getRaw(start), name));

			String cl = m.group("class");
			String level = m.group("level");

			if (level == null) {
				PlainTextComponent ptc = new PlainTextComponent(
						Text.literal("Player is dead").formatted(Formatting.RED));
				this.addComponent(ptc);
			} else {
				DungeonClass dungeonClass = DungeonClass.from(cl);

				Formatting clf = Formatting.GRAY;
				ItemStack cli = dungeonClass.icon();
				if (dungeonClass != DungeonClass.UNKNOWN) {
					clf = Formatting.LIGHT_PURPLE;
					cl += " " + m.group("level");
				}

				Text clazz = Text.literal("Class: ").append(Text.literal(cl).formatted(clf));
				this.addComponent(Components.iconTextComponent(cli, clazz));
			}
		}

		this.addSimpleIcoText(Ico.CLOCK, "Ult Cooldown:", Formatting.GOLD, start + 1);
		this.addSimpleIcoText(Ico.POTION, "Revives:", Formatting.DARK_PURPLE, start + 2);
	}
}
