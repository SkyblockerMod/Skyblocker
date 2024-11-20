package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows your dungeon essences (dungeon hub only)
@RegisterWidget
public class EssenceWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Essences").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	public EssenceWidget() {
		super("Essence", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		for (Text line : lines) {
			switch (line.getString().toLowerCase()) {
				case String s when s.contains("wither") -> this.addComponent(new IcoTextComponent(Ico.WITHER, line));
				case String s when s.contains("spider") -> this.addComponent(new IcoTextComponent(Ico.STRING, line));
				case String s when s.contains("undead") -> this.addComponent(new IcoTextComponent(Ico.FLESH, line));
				case String s when s.contains("dragon") -> this.addComponent(new IcoTextComponent(Ico.DRAGON, line));
				case String s when s.contains("gold") -> this.addComponent(new IcoTextComponent(Ico.GOLD, line));
				case String s when s.contains("diamond") -> this.addComponent(new IcoTextComponent(Ico.DIAMOND, line));
				case String s when s.contains("ice") -> this.addComponent(new IcoTextComponent(Ico.ICE, line));
				case String s when s.contains("crimson") -> this.addComponent(new IcoTextComponent(Ico.REDSTONE, line));
				default -> this.addComponent(new PlainTextComponent(line));
			}
		}
	}
}
