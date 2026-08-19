package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Location;

/// This widget shows info about the secrets of the dungeon.
@RegisterWidget
public class DungeonSecretWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Discoveries").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
	private static final Pattern DISCOVERIES = Pattern.compile("Discoveries: (\\d+)");

	public DungeonSecretWidget() {
		super("Dungeon Discoveries", TITLE, TextColor.BLUE.getValue(), Location.DUNGEON);
	}

	@Override
	public void updateContent(PlayerListManager.Widget ignored) {
		if (!DungeonScore.isDungeonStarted()) {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", ChatFormatting.YELLOW, 30);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", ChatFormatting.YELLOW, 31);
		} else if (PlayerListManager.regexAt(31, DISCOVERIES) != null) {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", ChatFormatting.YELLOW, 32);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", ChatFormatting.YELLOW, 33);
		} else {
			this.addSimpleIcoText(Ico.CHEST, "Secrets:", ChatFormatting.YELLOW, 31);
			this.addSimpleIcoText(Ico.SKULL, "Crypts:", ChatFormatting.YELLOW, 32);
		}
	}
}
