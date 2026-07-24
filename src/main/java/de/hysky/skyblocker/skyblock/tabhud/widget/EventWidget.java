package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.List;

// this widget shows info about ongoing events (e.g. election)
@RegisterWidget
public class EventWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Event Info").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);


	public EventWidget() {
		super("Event", TITLE, TextColor.YELLOW.getValue());
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		List<Component> lines = widget.lines();
		if (!widget.detail().getString().isEmpty()) this.addElement(Elements.iconTextComponent(Ico.NTAG, widget.detail()));
		if (!lines.isEmpty()) this.addElement(Elements.iconTextComponent(Ico.CLOCK, lines.getFirst()));
	}
}
