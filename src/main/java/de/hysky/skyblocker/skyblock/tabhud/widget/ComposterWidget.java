package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about the garden's composter
@RegisterWidget
public class ComposterWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Composter").formatted(Formatting.GREEN,
			Formatting.BOLD);

	public ComposterWidget() {
		super("Composter", TITLE, Formatting.GREEN.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {

		for (Text line : lines) {
			switch (line.getString().toLowerCase()) {
				case String s when s.contains("organic") -> this.addComponent(Components.iconTextComponent(Ico.SAPLING, line));
				case String s when s.contains("fuel") -> this.addComponent(Components.iconTextComponent(Ico.FURNACE, line));
				case String s when s.contains("time") -> this.addComponent(Components.iconTextComponent(Ico.CLOCK, line));
				case String s when s.contains("stored") -> this.addComponent(Components.iconTextComponent(Ico.COMPOSTER, line));
				default -> this.addComponent(new PlainTextComponent(line));
			}
		}
	}
}
