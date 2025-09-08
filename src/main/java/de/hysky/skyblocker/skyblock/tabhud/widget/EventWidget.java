package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about ongoing events (e.g. election)
@RegisterWidget
public class EventWidget extends TabHudWidget {
	private static final MutableText TITLE = Text.literal("Event Info").formatted(Formatting.YELLOW, Formatting.BOLD);


	public EventWidget() {
		super("Event", TITLE, Formatting.YELLOW.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		if (!lines.isEmpty()) this.addComponent(Components.iconTextComponent(Ico.NTAG, lines.getFirst()));
		if (lines.size() > 1) this.addComponent(Components.iconTextComponent(Ico.CLOCK, lines.get(1)));
	}
}
