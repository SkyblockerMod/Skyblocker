package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@RegisterWidget
public class PetWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Pet").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	private String prevString = "";
	private FlexibleItemStack icon = Ico.BONE;

	public PetWidget() {
		super("Pet", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	protected void updateContent(List<Component> lines) {
		for (Component line : lines) {
			String string = line.getString();
			if (string.contains("[") && string.contains("]")) {
				String[] split = string.split("]", 2);
				if (split.length < 2) {
					addComponent(new PlainTextElement(line));
					continue;
				}
				String petName = split[1].replace("✦", "").trim();
				if (!petName.equals(prevString)) {
					// FIXME performance
					icon = ItemRepository.getItemsStream().filter(stack -> {
						String string1 = stack.get(DataComponents.CUSTOM_NAME).getString();
						if (!string1.contains("]")) return false;
						String trim = string1.split("]")[1].trim();
						return trim.equals(petName);
					}).findFirst().orElse(Ico.BONE);
					prevString = petName;
				}
				addComponent(Elements.iconTextComponent(icon, line));

			} else addComponent(new PlainTextElement(line));
		}
	}
}
