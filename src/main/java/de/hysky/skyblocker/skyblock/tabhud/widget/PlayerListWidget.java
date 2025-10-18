package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlayerComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegisterWidget
public class PlayerListWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Players").formatted(Formatting.BOLD);

	public PlayerListWidget() {
		super("Players", TITLE, Formatting.AQUA.getColorValue());
	}

	@Override
	protected void updateContent(List<Text> lines, @Nullable List<PlayerListEntry> playerListEntries) {
		if (playerListEntries == null) {
			lines.forEach(text -> addComponent(new PlainTextComponent(text)));
		} else switch (SkyblockerConfigManager.get().uiAndVisuals.tabHud.nameSorting) {
			case DEFAULT -> playerListEntries.forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
			case null, default -> playerListEntries.stream().sorted(SkyblockerConfigManager.get().uiAndVisuals.tabHud.nameSorting.comparator).forEach(playerListEntry -> addComponent(new PlayerComponent(playerListEntry)));
		}
	}

	@Override
	protected void updateContent(List<Text> lines) {}
}
