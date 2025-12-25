package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static java.util.Map.entry;

// this widget shows info about the current jacob's contest (garden only)
@RegisterWidget
public class JacobsContestWidget extends TabHudWidget {

	private static final MutableComponent TITLE = Component.literal("Jacob's Contest").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);

	private static final Pattern CROP_PATTERN = Pattern.compile("(?<fortune>[☘○]) (?<crop>.+?)(?: ◆ )?(?<percentage>Top [\\d.]+%)?");

	// Ordered the same as "Unique Brackets Reached" in Anita NPC shop
	public static final Map<String, ItemStack> FARM_DATA = Map.ofEntries(
			entry("Wheat", new ItemStack(Items.WHEAT)),
			entry("Carrot", new ItemStack(Items.CARROT)),
			entry("Potato", new ItemStack(Items.POTATO)),
			entry("Pumpkin", new ItemStack(Items.PUMPKIN)),
			entry("Melon Slice", new ItemStack(Items.MELON_SLICE)),
			entry("Mushroom", new ItemStack(Items.RED_MUSHROOM)),
			entry("Cactus", new ItemStack(Items.CACTUS)),
			entry("Sugar Cane", new ItemStack(Items.SUGAR_CANE)),
			entry("Nether Wart", new ItemStack(Items.NETHER_WART)),
			entry("Cocoa Beans", new ItemStack(Items.COCOA_BEANS)),
			entry("Sunflower", ItemRepository.getItemStack("DOUBLE_PLANT", new ItemStack(Items.SUNFLOWER))),
			entry("Moonflower", ItemRepository.getItemStack("MOONFLOWER", new ItemStack(Items.BLUE_ORCHID))),
			entry("Wild Rose", ItemRepository.getItemStack("WILD_ROSE", new ItemStack(Items.ROSE_BUSH)))
	);

	public JacobsContestWidget() {
		super("Jacob's Contest", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		for (Component line : lines) {
			String string = line.getString();
			if (string.endsWith("left") || string.contains("Starts")) this.addComponent(Components.iconTextComponent(Ico.CLOCK, line));
			else {
				Matcher matcher = CROP_PATTERN.matcher(string);
				if (matcher.matches()) {
					String crop = matcher.group("crop");
					String percentage = matcher.group("percentage");
					MutableComponent cropText = Component.empty().append(crop);
					if (matcher.group("fortune").equals("☘")) cropText.append(Component.literal(" ☘").withStyle(ChatFormatting.GOLD));

					this.addComponent(Components.iconTextComponent(FARM_DATA.get(crop), cropText));
					if (percentage != null) this.addComponent(new PlainTextComponent(Component.literal(percentage)));
				} else this.addComponent(new PlainTextComponent(line));
			}
		}
	}
}
