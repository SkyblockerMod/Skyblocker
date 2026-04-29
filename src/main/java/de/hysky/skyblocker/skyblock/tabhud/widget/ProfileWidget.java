package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

// this widget shows info about your profile and bank
@RegisterWidget
public class ProfileWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Profile").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);

	public ProfileWidget() {
		super("Profile", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		this.addElement(Elements.iconTextComponent(Ico.SIGN, Component.literal("Profile: ").append(widget.detail())));
		for (Component text : widget.lines()) {
			switch (text.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("bank") -> this.addElement(Elements.iconTextComponent(Ico.GOLD, text));
				case String s when s.contains("interest") -> this.addElement(Elements.iconTextComponent(Ico.CLOCK, text));
				case String s when s.contains("pet") -> this.addElement(Elements.iconTextComponent(Ico.BONE, text));
				case String s when s.contains("sb level") -> this.addElement(Elements.iconTextComponent(Ico.EXPERIENCE_BOTTLE, text));
				default -> this.addElement(new PlainTextElement(text));
			}
		}

	}
}
