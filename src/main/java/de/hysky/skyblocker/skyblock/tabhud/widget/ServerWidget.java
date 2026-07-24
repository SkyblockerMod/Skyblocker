package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;

// this widget shows info about "generic" servers.
// a server is "generic", when only name, server ID and gems are shown
// in the third column of the tab HUD
@RegisterWidget
public class ServerWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Server Info").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);

	public ServerWidget() {
		super("Area", TITLE, TextColor.DARK_AQUA.getValue());
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		this.addElement(Elements.iconTextComponent(Ico.MAP, Component.literal("Area: ").append(widget.detail().copy().withStyle(ChatFormatting.DARK_AQUA))));
		for (Component text : widget.lines()) {
			String string = text.getString();
			switch (string.toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("server") -> this.addSimpleIcoText(Ico.NTAG, "Server ID:", ChatFormatting.GRAY, string.split(":", 2)[1]);
				case String s when s.contains("gems") -> this.addElement(Elements.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("crystals") -> this.addElement(Elements.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("copper") -> this.addElement(Elements.iconTextComponent(Ico.COPPER, text));
				case String s when s.contains("garden") -> this.addElement(Elements.iconTextComponent(Ico.EXPERIENCE_BOTTLE, text));
				case String s when s.contains("fairy") -> this.addElement(Elements.iconTextComponent(ItemRepository.getItemStack("PLACEABLE_FAIRY_SOUL_RIFT", Ico.FAIRY_SOUL), text));
				case String s when s.contains("rain") -> this.addElement(Elements.iconTextComponent(Ico.WATER, text));
				case String s when s.contains("brood") -> this.addElement(Elements.iconTextComponent(Ico.SPIDER_EYE, text));
				default -> this.addElement(new PlainTextElement(text));
			}
		}
	}
}
