package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.JsonValueOutput;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Players").withStyle(ChatFormatting.BOLD);

	private UIAndVisualsConfig.NameSorting nameSorting = UIAndVisualsConfig.NameSorting.DEFAULT;

	public PlayerListWidget() {
		super("Players", TITLE, TextColor.AQUA.getValue());
	}

	@Override
	protected void updateContent(PlayerListManager.Widget widget) {
		widget.playerListEntries().stream().sorted(nameSorting.comparator).forEach(playerListEntry -> addElement(new PlayerElement(playerListEntry)));
	}

	@Override
	protected void updateConfigContentTab(ElementCollector collector) {
		Random random = new Random(1234);
		List<PlayerInfo> infos = new ArrayList<>(15);

		TextColor[] colors = {TextColor.AQUA, TextColor.GREEN, TextColor.GRAY};
		for (int i = 0; i < 3; i++) {
			TextColor color = colors[i];
			int[] array = random.ints(5, 0, 500).sorted().toArray();
			ArrayUtils.reverse(array);
			for (int j = 0; j < 5; j++) {
				GameProfile profile = new GameProfile(UUID.randomUUID(), String.valueOf((char) ('a' + i * 5 + j)));
				PlayerInfo info = new PlayerInfo(profile, false);
				StringBuilder builder = new StringBuilder("Player ");
				random.ints(3, 'A', 'D').forEach(builder::appendCodePoint);
				info.setTabListDisplayName(Component.literal("[" + array[j] + "] ").append(Component.literal(builder.toString()).withColor(color)));
				infos.add(info);
			}
		}
		infos.stream().sorted(nameSorting.comparator).forEach(playerListEntry -> collector.addElement(new PlayerElement(playerListEntry)));
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.enumButton(UIAndVisualsConfig.NameSorting.class, Component.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting"), v -> nameSorting = v, nameSorting)
				.tooltip(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.nameSorting.@Tooltip"));
	}

	@Override
	public void load(JsonValueInput input) {
		super.load(input);
		nameSorting = input.read("name_sorting", UIAndVisualsConfig.NameSorting.CODEC).orElse(UIAndVisualsConfig.NameSorting.DEFAULT);
	}

	@Override
	public void save(JsonValueOutput output) {
		super.save(output);
		output.write("name_sorting", UIAndVisualsConfig.NameSorting.CODEC, nameSorting);
	}
}
