package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;

/// This widget shows a list of obtained dungeon buffs.
@RegisterWidget
public class DungeonBuffWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Dungeon Buffs").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);

	public DungeonBuffWidget() {
		super("Dungeon Buffs", TITLE, TextColor.GREEN.getValue());
	}

	@Override
	public void updateContent(List<Component> ignored) {

		String footer = PlayerListManager.getFooter();

		if (footer == null || !footer.contains("Dungeon Buffs")) {
			this.addComponent(new PlainTextElement(Component.literal("No data").withStyle(ChatFormatting.GRAY)));
			return;
		}

		String[] lines = footer.split("Dungeon Buffs")[1].split("\n");

		if (!lines[1].startsWith("Blessing")) {
			this.addComponent(new PlainTextElement(Component.literal("No buffs found!").withStyle(ChatFormatting.GRAY)));
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
			this.addComponent(new PlainTextElement(Component.literal(line).withColor(color)));
		}
	}

	public int getBlessingColor(String blessing) {
		if (blessing.contains("Life")) return TextColor.LIGHT_PURPLE.getValue();
		if (blessing.contains("Power")) return TextColor.RED.getValue();
		if (blessing.contains("Stone")) return TextColor.GREEN.getValue();
		if (blessing.contains("Time")) return 0xAFB8C1;
		if (blessing.contains("Wisdom")) return TextColor.AQUA.getValue();

		return 0xFFFFFF;
	}

}
