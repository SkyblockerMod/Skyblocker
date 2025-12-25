package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

@RegisterWidget
public class PetWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Pet").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	private String prevString = "";
	private ItemStack icon = Ico.BONE;

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
					addComponent(new PlainTextComponent(line));
					continue;
				}
				String petName = split[1].replace("✦", "").trim();
				if (!petName.equals(prevString)) {
					icon = ItemRepository.getItemsStream().filter(stack -> {
						String string1 = stack.getHoverName().getString();
						if (!string1.contains("]")) return false;
						String trim = string1.split("]")[1].trim();
						return trim.equals(petName);
					}).findFirst().orElse(Ico.BONE);
					prevString = petName;
				}
				addComponent(Components.iconTextComponent(icon, line));

			} else addComponent(new PlainTextComponent(line));
		}
	}
}
