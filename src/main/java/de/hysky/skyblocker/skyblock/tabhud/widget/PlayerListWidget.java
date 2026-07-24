package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlayerElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Players").withStyle(ChatFormatting.BOLD);

	public PlayerListWidget() {
		super("Players", TITLE, TextColor.AQUA.getValue());
	}

	@Override
	protected void updateContent(PlayerListManager.Widget widget) {
		widget.playerListEntries().stream().sorted(SkyblockerConfigManager.get().uiAndVisuals.tabHud.nameSorting.comparator).forEach(playerListEntry -> addElement(new PlayerElement(playerListEntry)));
	}
}
