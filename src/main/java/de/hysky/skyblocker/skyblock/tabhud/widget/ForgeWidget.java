package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoFatTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows what you're forging right now.
// for locked slots, the unlock requirement is shown
@RegisterWidget
public class ForgeWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Forges").formatted(Formatting.DARK_AQUA,
			Formatting.BOLD);

	public ForgeWidget() {
		super("Forges", TITLE, Formatting.DARK_AQUA.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		boolean b = lines.getFirst().getString().trim().startsWith("(");
		for (int i = b ? 1 : 0, slot = 1; i < lines.size(); i++, slot++) {
			String trim = lines.get(i).getString().trim();

			Component c;
			Text l1, l2;

			switch (trim.substring(3)) {
				case "LOCKED" -> {
					l1 = Text.literal("Locked").formatted(Formatting.RED);
					l2 = switch (slot) {
						case 3 -> Text.literal("Needs HotM 3").formatted(Formatting.GRAY);
						case 4 -> Text.literal("Needs HotM 4").formatted(Formatting.GRAY);
						case 5 -> Text.literal("Needs HotM 5").formatted(Formatting.GRAY);
						case 6 -> Text.literal("Needs HotM 6").formatted(Formatting.GRAY);
						case 7 -> Text.literal("Needs HotM 7").formatted(Formatting.GRAY);
						default -> Text.literal("This message should not appear").formatted(Formatting.RED, Formatting.BOLD);
					};
					c = new IcoFatTextComponent(Ico.BARRIER, l1, l2);
				}
				case "EMPTY" -> {
					l1 = Text.literal("Empty").formatted(Formatting.GRAY);
					c = new IcoTextComponent(Ico.FURNACE, l1);
				}
				default -> {
					String[] parts = trim.split(": ");
					if (parts.length != 2) {
						c = new IcoFatTextComponent();
					} else {
						l1 = Text.literal(parts[0].substring(3)).formatted(Formatting.YELLOW);
						if (parts[1].equals("Ready!")) {
							l2 = Text.literal("Done!").formatted(Formatting.GREEN);
						} else {
							l2 = Text.literal("Done in: ").formatted(Formatting.GRAY).append(Text.literal(parts[1]).formatted(Formatting.WHITE));
						}
						c = new IcoFatTextComponent(Ico.CAMPFIRE, l1, l2);
					}
				}
			}
			this.addComponent(c);
		}
	}

}
