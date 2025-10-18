package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about... something?
// related to downed people in dungeons, not sure what this is supposed to show
@RegisterWidget
public class DungeonDownedWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Downed").formatted(Formatting.DARK_PURPLE,
			Formatting.BOLD);

	public DungeonDownedWidget() {
		super("Downed", TITLE, Formatting.DARK_PURPLE.getColorValue(), Location.DUNGEON);
		cacheForConfig = false;
	}

	@Override
	public void updateContent() {
		String down = PlayerListManager.strAt(21);
		if (down == null) {
			this.addComponent(Components.iconTextComponent());
		} else {

			Formatting format = Formatting.RED;
			if (down.endsWith("NONE")) {
				format = Formatting.GRAY;
			}
			int idx = down.indexOf(": ");
			Text downed = (idx == -1) ? null
					: simpleEntryText(down.substring(idx + 2), "Downed: ", format);
			this.addComponent(Components.iconTextComponent(Ico.SKULL, downed));
		}

		this.addSimpleIcoText(Ico.CLOCK, "Time:", Formatting.GRAY, 22);
		this.addSimpleIcoText(Ico.POTION, "Revive:", Formatting.GRAY, 23);
	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				Components.iconTextComponent(Ico.SKULL, simpleEntryText("NONE", "Downed: ", Formatting.GRAY)),
				Components.iconTextComponent(Ico.CLOCK, simpleEntryText("N/A", "Time:", Formatting.GRAY)),
				Components.iconTextComponent(Ico.POTION, simpleEntryText("N/A", "Revive:", Formatting.GRAY))
		);
	}

	@Override
	protected void updateContent(List<Text> lines) {}
}
