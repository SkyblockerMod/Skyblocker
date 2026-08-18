package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Location;

// this widget shows a list of obtained dungeon buffs
@RegisterWidget
public class DungeonBuffWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Dungeon Buffs").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);

	public DungeonBuffWidget() {
		super("Dungeon Buffs", TITLE, ChatFormatting.GREEN.getColor(), new Information("dungeon_buffs", Component.literal("Dungeon Buffs"), Location.DUNGEON));
	}

	@Override
	public void updateContent(PlayerListManager.Widget ignored) {

		String footer = PlayerListManager.getFooter();

		if (footer == null || !footer.contains("Dungeon Buffs")) {
			this.addElement(new PlainTextElement(Component.literal("No data").withStyle(ChatFormatting.GRAY)));
			return;
		}

		String[] lines = footer.split("Dungeon Buffs")[1].split("\n");

		if (!lines[1].startsWith("Blessing")) {
			this.addElement(new PlainTextElement(Component.literal("No buffs found!").withStyle(ChatFormatting.GRAY)));
			return;
		}

		//Filter out text unrelated to blessings
		lines = Arrays.stream(lines).filter(s -> s.contains("Blessing")).toArray(String[]::new);

		//Alphabetically sort the blessings
		Arrays.sort(lines, Comparator.comparing(String::toLowerCase));

		for (String line : lines) {
			if (line.length() < 3) { // empty line is §s
				break;
			}
			int color = getBlessingColor(line);
			this.addElement(new PlainTextElement(Component.literal(line).withColor(color)));
		}
	}

	public int getBlessingColor(String blessing) {
		if (blessing.contains("Life")) return ChatFormatting.LIGHT_PURPLE.getColor();
		if (blessing.contains("Power")) return ChatFormatting.RED.getColor();
		if (blessing.contains("Stone")) return ChatFormatting.GREEN.getColor();
		if (blessing.contains("Time")) return 0xAFB8C1;
		if (blessing.contains("Wisdom")) return ChatFormatting.AQUA.getColor();

		return 0xFFFFFF;
	}

}
