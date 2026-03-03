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

// this widget shows your dungeon essences (dungeon hub only)
@RegisterWidget
public class EssenceWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Essences").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	public EssenceWidget() {
		super("Essence", TITLE, ChatFormatting.DARK_AQUA.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		for (Component line : lines) {
			switch (line.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("wither") -> this.addComponent(Elements.iconTextComponent(Ico.WITHER, line));
				case String s when s.contains("spider") -> this.addComponent(Elements.iconTextComponent(Ico.STRING, line));
				case String s when s.contains("undead") -> this.addComponent(Elements.iconTextComponent(Ico.FLESH, line));
				case String s when s.contains("dragon") -> this.addComponent(Elements.iconTextComponent(Ico.DRAGON, line));
				case String s when s.contains("gold") -> this.addComponent(Elements.iconTextComponent(Ico.GOLD, line));
				case String s when s.contains("diamond") -> this.addComponent(Elements.iconTextComponent(Ico.DIAMOND, line));
				case String s when s.contains("ice") -> this.addComponent(Elements.iconTextComponent(Ico.ICE, line));
				case String s when s.contains("crimson") -> this.addComponent(Elements.iconTextComponent(Ico.REDSTONE, line));
				default -> this.addComponent(new PlainTextElement(line));
			}
		}
	}
}
