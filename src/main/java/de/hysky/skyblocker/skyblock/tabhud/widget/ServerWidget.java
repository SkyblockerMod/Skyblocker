package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about "generic" servers.
// a server is "generic", when only name, server ID and gems are shown
// in the third column of the tab HUD
@RegisterWidget
public class ServerWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	public ServerWidget() {
		super("Area", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		this.addComponent(Components.iconTextComponent(Ico.MAP, Text.literal("Area: ").append(lines.getFirst().copy().formatted(Formatting.DARK_AQUA))));
		for (int i = 1; i < lines.size(); i++) {
			Text text = lines.get(i);
			String string = text.getString();
			switch (string.toLowerCase()) {
				case String s when s.contains("server") -> this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, string.split(":", 2)[1]);
				case String s when s.contains("gems") -> this.addComponent(Components.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("crystals") -> this.addComponent(Components.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("copper") -> this.addComponent(Components.iconTextComponent(Ico.COPPER, text));
				case String s when s.contains("garden") -> this.addComponent(Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, text));
				case String s when s.contains("fairy") -> this.addComponent(Components.iconTextComponent(Ico.FAIRY_SOUL, text));
				case String s when s.contains("rain") -> this.addComponent(Components.iconTextComponent(Ico.WATER, text));
				case String s when s.contains("brood") -> this.addComponent(Components.iconTextComponent(Ico.SPIDER_EYE, text));
				default -> this.addComponent(new PlainTextComponent(text));
			}
		}
	}
}
