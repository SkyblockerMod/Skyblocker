package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about the garden's composter
@RegisterWidget
public class ComposterWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Composter").withStyle(ChatFormatting.GREEN,
			ChatFormatting.BOLD);

	public ComposterWidget() {
		super("Composter", TITLE, ChatFormatting.GREEN.getColor(), Location.GARDEN);
	}

	@Override
	public void updateContent(List<Component> lines) {

		for (Component line : lines) {
			switch (line.getString().toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("organic") -> this.addComponent(Components.iconTextComponent(Ico.SAPLING, line));
				case String s when s.contains("fuel") -> this.addComponent(Components.iconTextComponent(Ico.FURNACE, line));
				case String s when s.contains("time") -> this.addComponent(Components.iconTextComponent(Ico.CLOCK, line));
				case String s when s.contains("stored") -> this.addComponent(Components.iconTextComponent(Ico.COMPOSTER, line));
				default -> this.addComponent(new PlainTextComponent(line));
			}
		}
	}
}
