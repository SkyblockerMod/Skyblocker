package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about the secrets of the dungeon
@RegisterWidget
public class DungeonSecretWidget extends TabHudWidget {

	private static final MutableComponent TITLE = net.minecraft.network.chat.Component.literal("Discoveries").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
	private static final Pattern DISCOVERIES = Pattern.compile("Discoveries: (\\d+)");

	public DungeonSecretWidget() {
		super("Discoveries", TITLE, ChatFormatting.DARK_PURPLE.getColor(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
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

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				Components.iconTextComponent(Ico.CHEST, simpleEntryText("0", "Secrets:", ChatFormatting.YELLOW)),
				Components.iconTextComponent(Ico.SKULL, simpleEntryText("0", "Crypts:", ChatFormatting.YELLOW))
		);
	}

	@Override
	protected void updateContent(List<net.minecraft.network.chat.Component> lines) {}
}
