package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
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
	protected void updateContent(List<Component> lines) {
		String string = lines.getFirst().getString().replaceAll("[()]", "");
		addComponent(new PlainTextComponent(
						Component.literal(string).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD).append(
								Component.literal(" visitor(s)").withStyle(ChatFormatting.WHITE))
				)
		);

		for (int i = 1; i < lines.size(); i++) {
			addComponent(new PlainTextComponent(lines.get(i)));
		}
	}
}
