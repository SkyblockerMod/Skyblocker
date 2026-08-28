package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SkyBlockIcons;

import static java.util.Map.entry;

// this widget shows info about the current jacob's contest (garden only)
@RegisterWidget
public class JacobsContestWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Jacob's Contest").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	private static final Pattern CROP_PATTERN = Pattern.compile(String.format("(?<fortune>[%s○]) (?<crop>.+?)(?: ◆ )?(?<percentage>Top [\\d.]+%%)?", SkyBlockIcons.FARMING_FORTUNE));

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
		super("Jacob's Contest", TITLE, ChatFormatting.YELLOW.getColor(), Location.HUB, Location.THE_FARMING_ISLAND, Location.GARDEN);
	}

	@Override
	public void updateContent(PlayerListManager.Widget widget) {
		if (widget.lines().isEmpty()) this.addElement(new PlainTextElement(widget.detail()));
		if (widget.detail().getString().contains("left")) this.addElement(Elements.iconTextComponent(Ico.CLOCK, widget.detail()));
		for (Component line : widget.lines()) {
			String string = line.getString();
			if (string.contains("Starts")) this.addElement(Elements.iconTextComponent(Ico.CLOCK, line));
			else {
				Matcher matcher = CROP_PATTERN.matcher(string);
				if (matcher.matches()) {
					String crop = matcher.group("crop");
					String percentage = matcher.group("percentage");
					MutableComponent cropText = Component.empty().append(crop);
					if (matcher.group("fortune").equals(String.valueOf(SkyBlockIcons.FARMING_FORTUNE))) cropText.append(Component.literal(" " + SkyBlockIcons.FARMING_FORTUNE).withStyle(ChatFormatting.GOLD));

					this.addElement(Elements.iconTextComponent(FARM_DATA.get(crop), cropText));
					if (percentage != null) this.addElement(new PlainTextElement(Component.literal(percentage)));
				} else this.addElement(new PlainTextElement(line));
			}
		}
	}

	@Override
	protected void updateConfigContentTab(ElementCollector collector) {
		collector.addElement(Elements.iconTextComponent(Ico.CLOCK, Component.literal("Starts in: ").append(Component.literal("???").withStyle(ChatFormatting.YELLOW))));
		FlexibleItemStack[] strings = FARM_DATA.values().toArray(FlexibleItemStack[]::new);
		for (int i = 0; i < 3; i++) {
			this.addElement(Elements.iconTextComponent(strings[i], Component.literal("Crop " + (i + 1))));
		}
	}
}
