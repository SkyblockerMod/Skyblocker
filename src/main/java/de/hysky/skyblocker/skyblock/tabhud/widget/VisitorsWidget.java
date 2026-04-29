package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@RegisterWidget
public class VisitorsWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Visitors").withStyle(ChatFormatting.AQUA,
			ChatFormatting.BOLD);

	public VisitorsWidget() {
		super("Visitors", TITLE, ChatFormatting.AQUA.getColor());
	}

	@Override
	protected void updateContent(PlayerListManager.Widget widget) {
		String string = widget.detail().getString().replaceAll("[()]", "");
		addElement(new PlainTextElement(
						Component.literal(string).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD).append(
								Component.literal(" visitor(s)").withStyle(ChatFormatting.WHITE))
				)
		);

		for (Component line : widget.lines()) {
			addElement(new PlainTextElement(line));
		}
	}
}
