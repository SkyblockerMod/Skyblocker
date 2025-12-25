package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about... something?
// related to downed people in dungeons, not sure what this is supposed to show
@RegisterWidget
public class DungeonDownedWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Downed").withStyle(ChatFormatting.DARK_PURPLE,
			ChatFormatting.BOLD);

	public DungeonDownedWidget() {
		super("Dungeon Downed", TITLE, ChatFormatting.DARK_PURPLE.getColor());
	}

	@Override
	public void updateContent(List<Component> ignored) {
		String down = PlayerListManager.strAt(21);
		if (down == null) {
			this.addComponent(Components.iconTextComponent());
		} else {

			ChatFormatting format = ChatFormatting.RED;
			if (down.endsWith("NONE")) {
				format = ChatFormatting.GRAY;
			}
			int idx = down.indexOf(": ");
			Component downed = (idx == -1) ? null
					: simpleEntryText(down.substring(idx + 2), "Downed: ", format);
			this.addComponent(Components.iconTextComponent(Ico.SKULL, downed));
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", ChatFormatting.GRAY, 22);
		this.addSimpleIcoText(Ico.POTION, "Revive:", ChatFormatting.GRAY, 23);
	}
}
