package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about ongoing events (e.g. election)
@RegisterWidget
public class EventWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Event Info").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);


	public EventWidget() {
		super("Event", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		if (!lines.isEmpty()) this.addComponent(Components.iconTextComponent(Ico.NTAG, lines.getFirst()));
		if (lines.size() > 1) this.addComponent(Components.iconTextComponent(Ico.CLOCK, lines.get(1)));
	}
}
