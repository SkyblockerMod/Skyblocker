package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

// this widget shows what you're forging right now.
// for locked slots, the unlock requirement is shown
@RegisterWidget
public class ForgeWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Forges").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	public ForgeWidget() {
		super("Forges", TITLE, ChatFormatting.DARK_AQUA.getColor());
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		List<Component> lines = widget.lines();
		for (int i = 0, slot = 1; i < lines.size(); i++, slot++) {
			String trim = lines.get(i).getString().trim();

			Element c;
			Component l1, l2;

			switch (trim.substring(3)) {
				case "LOCKED" -> {
					l1 = Component.literal("Locked").withStyle(ChatFormatting.RED);
					l2 = switch (slot) {
						case 3 -> Component.literal("Needs HotM 3").withStyle(ChatFormatting.GRAY);
						case 4 -> Component.literal("Needs HotM 4").withStyle(ChatFormatting.GRAY);
						case 5 -> Component.literal("Needs HotM 5").withStyle(ChatFormatting.GRAY);
						case 6 -> Component.literal("Needs HotM 6").withStyle(ChatFormatting.GRAY);
						case 7 -> Component.literal("Needs HotM 7").withStyle(ChatFormatting.GRAY);
						default -> Component.literal("This message should not appear").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
					};
					c = Elements.iconFatTextComponent(Ico.BARRIER, l1, l2);
				}
				case "EMPTY" -> {
					l1 = Component.literal("Empty").withStyle(ChatFormatting.GRAY);
					c = Elements.iconTextComponent(Ico.FURNACE, l1);
				}
				default -> {
					String[] parts = trim.split(": ");
					if (parts.length != 2) {
						c = Elements.iconFatTextComponent();
					} else {
						l1 = Component.literal(parts[0].substring(3)).withStyle(ChatFormatting.YELLOW);
						if (parts[1].equals("Ready!")) {
							l2 = Component.literal("Done!").withStyle(ChatFormatting.GREEN);
						} else {
							l2 = Component.literal("Done in: ").withStyle(ChatFormatting.GRAY).append(Component.literal(parts[1]).withStyle(ChatFormatting.WHITE));
						}
						c = Elements.iconFatTextComponent(Ico.CAMPFIRE, l1, l2);
					}
				}
			}
			this.addElement(c);
		}
	}
}
