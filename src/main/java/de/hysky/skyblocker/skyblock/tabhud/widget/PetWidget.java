package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@RegisterWidget
public class PetWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Pet").formatted(Formatting.YELLOW,
			Formatting.BOLD);

	private String prevString = "";
	private ItemStack icon = Ico.BONE;

	public PetWidget() {
		super("Pet", TITLE, Formatting.YELLOW.getColorValue());
	}

	@Override
	protected void updateContent(List<Text> lines) {
		for (Text line : lines) {
			String string = line.getString();
			if (string.contains("[") && string.contains("]")) {
				String[] split = string.split("]", 2);
				if (split.length < 2) {
					addComponent(new PlainTextComponent(line));
					continue;
				}
				String petName = split[1].replace("✦", "").trim();
				if (!petName.equals(prevString)) {
					icon = ItemRepository.getItemsStream().filter(stack -> {
						String trim = stack.getName().getString().trim();
						return trim.contains("]") && trim.endsWith(petName);
					}).findFirst().orElse(Ico.BONE);
					prevString = petName;
				}
				addComponent(new IcoTextComponent(icon, line));

			} else addComponent(new PlainTextComponent(line));
		}
	}
}
