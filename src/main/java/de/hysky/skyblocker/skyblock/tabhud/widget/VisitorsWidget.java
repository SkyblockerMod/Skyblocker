package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@RegisterWidget
public class VisitorsWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Visitors").formatted(Formatting.AQUA,
			Formatting.BOLD);

	public VisitorsWidget() {
		super("Visitors", TITLE, Formatting.AQUA.getColorValue());
	}

	@Override
	protected void updateContent(List<Text> lines) {
		String string = lines.getFirst().getString().replaceAll("[()]", "");
		addComponent(new PlainTextComponent(
						Text.literal(string).formatted(Formatting.YELLOW, Formatting.BOLD).append(
								Text.literal(" visitor(s)").formatted(Formatting.WHITE))
				)
		);

		for (int i = 1; i < lines.size(); i++) {
			addComponent(new PlainTextComponent(lines.get(i)));
		}
	}
}
