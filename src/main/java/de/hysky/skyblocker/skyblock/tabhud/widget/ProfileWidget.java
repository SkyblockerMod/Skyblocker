package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about your profile and bank
@RegisterWidget
public class ProfileWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Profile").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);

	public ProfileWidget() {
		super("Profile", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		this.addComponent(Elements.iconTextComponent(Ico.SIGN, Component.literal("Profile: ").append(lines.getFirst())));
		for (int i = 1; i < lines.size(); i++) {
			Component text = lines.get(i);
			switch (text.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("bank") -> this.addComponent(Elements.iconTextComponent(Ico.GOLD, text));
				case String s when s.contains("interest") -> this.addComponent(Elements.iconTextComponent(Ico.CLOCK, text));
				case String s when s.contains("pet") -> this.addComponent(Elements.iconTextComponent(Ico.BONE, text));
				case String s when s.contains("sb level") -> this.addComponent(Elements.iconTextComponent(Ico.EXPERIENCE_BOTTLE, text));
				default -> this.addComponent(new PlainTextElement(text));
			}
		}

	}
}
