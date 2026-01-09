package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

// this widget shows info about "generic" servers.
// a server is "generic", when only name, server ID and gems are shown
// in the third column of the tab HUD
@RegisterWidget
public class ServerWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Server Info").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD);

	public ServerWidget() {
		super("Area", TITLE, ChatFormatting.DARK_AQUA.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		this.addComponent(Components.iconTextComponent(Ico.MAP, Component.literal("Area: ").append(lines.getFirst().copy().withStyle(ChatFormatting.DARK_AQUA))));
		for (int i = 1; i < lines.size(); i++) {
			Component text = lines.get(i);
			String string = text.getString();
			switch (string.toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("server") -> this.addSimpleIcoText(Ico.NTAG, "Server ID:", ChatFormatting.GRAY, string.split(":", 2)[1]);
				case String s when s.contains("gems") -> this.addComponent(Components.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("crystals") -> this.addComponent(Components.iconTextComponent(Ico.EMERALD, text));
				case String s when s.contains("copper") -> this.addComponent(Components.iconTextComponent(Ico.COPPER, text));
				case String s when s.contains("garden") -> this.addComponent(Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, text));
				case String s when s.contains("fairy") -> this.addComponent(Components.iconTextComponent(ItemRepository.getItemStack("PLACEABLE_FAIRY_SOUL_RIFT", Ico.FAIRY_SOUL), text));
				case String s when s.contains("rain") -> this.addComponent(Components.iconTextComponent(Ico.WATER, text));
				case String s when s.contains("brood") -> this.addComponent(Components.iconTextComponent(Ico.SPIDER_EYE, text));
				default -> this.addComponent(new PlainTextComponent(text));
			}
		}
	}
}
