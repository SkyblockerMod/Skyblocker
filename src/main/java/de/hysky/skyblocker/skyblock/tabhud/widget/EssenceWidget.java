package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

// this widget shows your dungeon essences (dungeon hub only)
@RegisterWidget
public class EssenceWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Essences").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	public EssenceWidget() {
		super("Essence", TITLE, ChatFormatting.DARK_AQUA.getColor(), Location.DUNGEON_HUB);
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		for (Component line : widget.lines()) {
			switch (line.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("wither") -> this.addElement(Elements.iconTextComponent(Ico.WITHER, line));
				case String s when s.contains("spider") -> this.addElement(Elements.iconTextComponent(Ico.STRING, line));
				case String s when s.contains("undead") -> this.addElement(Elements.iconTextComponent(Ico.FLESH, line));
				case String s when s.contains("dragon") -> this.addElement(Elements.iconTextComponent(Ico.DRAGON, line));
				case String s when s.contains("gold") -> this.addElement(Elements.iconTextComponent(Ico.GOLD, line));
				case String s when s.contains("diamond") -> this.addElement(Elements.iconTextComponent(Ico.DIAMOND, line));
				case String s when s.contains("ice") -> this.addElement(Elements.iconTextComponent(Ico.ICE, line));
				case String s when s.contains("crimson") -> this.addElement(Elements.iconTextComponent(Ico.REDSTONE, line));
				default -> this.addElement(new PlainTextElement(line));
			}
		}
	}
}
