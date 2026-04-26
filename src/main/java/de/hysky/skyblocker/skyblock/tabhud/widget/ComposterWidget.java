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

// this widget shows info about the garden's composter
@RegisterWidget
public class ComposterWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Composter").withStyle(ChatFormatting.GREEN,
			ChatFormatting.BOLD);

	public ComposterWidget() {
		super("Composter", TITLE, ChatFormatting.GREEN.getColor(), new Information("composter", Component.literal("Composter"), Location.GARDEN));
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {

		for (Component line : widget.lines()) {
			switch (line.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("organic") -> this.addComponent(Elements.iconTextComponent(Ico.SAPLING, line));
				case String s when s.contains("fuel") -> this.addComponent(Elements.iconTextComponent(Ico.FURNACE, line));
				case String s when s.contains("time") -> this.addComponent(Elements.iconTextComponent(Ico.CLOCK, line));
				case String s when s.contains("stored") -> this.addComponent(Elements.iconTextComponent(Ico.COMPOSTER, line));
				default -> this.addComponent(new PlainTextElement(line));
			}
		}
	}
}
