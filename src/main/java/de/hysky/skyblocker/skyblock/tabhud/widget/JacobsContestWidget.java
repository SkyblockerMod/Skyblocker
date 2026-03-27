package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static java.util.Map.entry;

// this widget shows info about the current jacob's contest (garden only)
@RegisterWidget
public class JacobsContestWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Jacob's Contest").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	private static final Pattern CROP_PATTERN = Pattern.compile("(?<fortune>[☘○]) (?<crop>.+?)(?: ◆ )?(?<percentage>Top [\\d.]+%)?");

	// Ordered the same as "Unique Brackets Reached" in Anita NPC shop
	public static final Map<String, FlexibleItemStack> FARM_DATA = Map.ofEntries(
			entry("Wheat", Ico.WHEAT),
			entry("Carrot", Ico.CARROT),
			entry("Potato", Ico.POTATO),
			entry("Pumpkin", Ico.PUMPKIN),
			entry("Melon Slice", Ico.MELON_SLICE),
			entry("Mushroom", Ico.RED_MUSHROOM),
			entry("Cactus", Ico.CACTUS),
			entry("Sugar Cane", Ico.SUGAR_CANE),
			entry("Nether Wart", Ico.NETHER_WART),
			entry("Cocoa Beans", Ico.COCOA_BEANS),
			entry("Sunflower", ItemRepository.getItemStack("DOUBLE_PLANT", Ico.SUNFLOWER)),
			entry("Moonflower", ItemRepository.getItemStack("MOONFLOWER", Ico.BLUE_ORCHID)),
			entry("Wild Rose", ItemRepository.getItemStack("WILD_ROSE", Ico.ROSE_BUSH))
	);

	public JacobsContestWidget() {
		super("Jacob's Contest", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		for (Component line : lines) {
			String string = line.getString();
			if (string.endsWith("left") || string.contains("Starts")) this.addComponent(Elements.iconTextComponent(Ico.CLOCK, line));
			else {
				Matcher matcher = CROP_PATTERN.matcher(string);
				if (matcher.matches()) {
					String crop = matcher.group("crop");
					String percentage = matcher.group("percentage");
					MutableComponent cropText = Component.empty().append(crop);
					if (matcher.group("fortune").equals("☘")) cropText.append(Component.literal(" ☘").withStyle(ChatFormatting.GOLD));

					this.addComponent(Elements.iconTextComponent(FARM_DATA.get(crop), cropText));
					if (percentage != null) this.addComponent(new PlainTextElement(Component.literal(percentage)));
				} else this.addComponent(new PlainTextElement(line));
			}
		}
	}
}
