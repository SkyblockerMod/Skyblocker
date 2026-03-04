package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Players").withStyle(ChatFormatting.BOLD);

	public PlayerListWidget() {
		super("Players", TITLE, ChatFormatting.AQUA.getColor());
	}

	@Override
	protected void updateContent(List<Component> lines, @Nullable List<PlayerInfo> playerListEntries) {
		if (playerListEntries == null) {
			lines.forEach(text -> addComponent(new PlainTextElement(text)));
		} else if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.nameSorting == UIAndVisualsConfig.NameSorting.DEFAULT) {
			playerListEntries.forEach(playerListEntry -> addComponent(new PlayerElement(playerListEntry)));
		} else {
			playerListEntries.stream().sorted(SkyblockerConfigManager.get().uiAndVisuals.tabHud.nameSorting.comparator).forEach(playerListEntry -> addComponent(new PlayerElement(playerListEntry)));
		}
	}

	@Override
	protected void updateContent(List<Component> lines) {}
}
