package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows a list of obtained dungeon buffs
@RegisterWidget
public class DungeonBuffWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Dungeon Buffs").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	public DungeonBuffWidget() {
		super("Dungeon Buffs", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
		String footertext = PlayerListManager.getFooter();

		if (footertext == null || !footertext.contains("Dungeon Buffs")) {
			this.addComponent(new PlainTextComponent(Component.literal("No data").withStyle(ChatFormatting.GRAY)));
			return;
		}

		String interesting = footertext.split("Dungeon Buffs")[1];
		String[] lines = interesting.split("\n");

		if (!lines[1].startsWith("Blessing")) {
			this.addComponent(new PlainTextComponent(Component.literal("No buffs found!").withStyle(ChatFormatting.GRAY)));
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
			this.addComponent(new PlainTextComponent(Component.literal(line).withStyle(style -> style.withColor(color))));
		}

	}

	@Override
	protected List<de.hysky.skyblocker.skyblock.tabhud.widget.component.Component> getConfigComponents() {
		return List.of(new PlainTextComponent(net.minecraft.network.chat.Component.literal("Life Blessing").withStyle(ChatFormatting.LIGHT_PURPLE)));
	}

	@Override
	protected void updateContent(List<net.minecraft.network.chat.Component> lines) {}

	@SuppressWarnings("DataFlowIssue")
	public int getBlessingColor(String blessing) {
		if (blessing.contains("Life")) return ChatFormatting.LIGHT_PURPLE.getColor();
		if (blessing.contains("Power")) return ChatFormatting.RED.getColor();
		if (blessing.contains("Stone")) return ChatFormatting.GREEN.getColor();
		if (blessing.contains("Time")) return 0xAFB8C1;
		if (blessing.contains("Wisdom")) return ChatFormatting.AQUA.getColor();

		return 0xFFFFFF;
	}

}
