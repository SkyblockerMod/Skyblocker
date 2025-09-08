package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

// this widget shows info about the current jacob's contest (garden only)
@RegisterWidget
public class JacobsContestWidget extends TabHudWidget {

	private static final MutableText TITLE = Text.literal("Jacob's Contest").formatted(Formatting.YELLOW,
			Formatting.BOLD);

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
			entry("Cocoa Beans", new ItemStack(Items.COCOA_BEANS))
	);

	public JacobsContestWidget() {
		super("Jacob's Contest", TITLE, Formatting.YELLOW.getColorValue());
	}

	@Override
	public void updateContent(List<Text> lines) {
		for (Text line : lines) {
			String string = line.getString();
			if (string.endsWith("left") || string.contains("Starts")) this.addComponent(Components.iconTextComponent(Ico.CLOCK, line));
			else {
				Matcher matcher = CROP_PATTERN.matcher(string);
				if (matcher.matches()) {
					String crop = matcher.group("crop");
					String percentage = matcher.group("percentage");
					MutableText cropText = Text.empty().append(crop);
					if (matcher.group("fortune").equals("☘")) cropText.append(Text.literal(" ☘").formatted(Formatting.GOLD));

					this.addComponent(Components.iconTextComponent(FARM_DATA.get(crop), cropText));
					if (percentage != null) this.addComponent(new PlainTextComponent(Text.literal(percentage)));
				} else this.addComponent(new PlainTextComponent(line));
			}
		}
	}
}
