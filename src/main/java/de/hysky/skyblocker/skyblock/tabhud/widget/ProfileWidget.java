package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about your profile and bank
@RegisterWidget
public class ProfileWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Profile").formatted(Formatting.YELLOW, Formatting.BOLD);

	public ProfileWidget() {
		super("Profile", TITLE, Formatting.YELLOW.getColorValue());

	}

	@Override
	public void updateContent(List<Text> lines) {
		this.addComponent(new IcoTextComponent(Ico.SIGN, Text.literal("Profile: ").append(lines.getFirst())));
		for (int i = 1; i < lines.size(); i++) {
			Text text = lines.get(i);
			switch (text.getString().toLowerCase()) {
				case String s when s.contains("bank") -> this.addComponent(new IcoTextComponent(Ico.GOLD, text));
				case String s when s.contains("interest") -> this.addComponent(new IcoTextComponent(Ico.CLOCK, text));
				case String s when s.contains("pet") -> this.addComponent(new IcoTextComponent(Ico.BONE, text));
				default -> this.addComponent(new PlainTextComponent(text));
			}
		}

	}

}
