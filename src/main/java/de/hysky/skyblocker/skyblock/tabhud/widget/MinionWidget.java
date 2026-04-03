package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

// this widget shows info about minions placed on the home island
@RegisterWidget
public class MinionWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Minions").withStyle(ChatFormatting.DARK_AQUA,
			ChatFormatting.BOLD);

	private static final HashMap<String, FlexibleItemStack> MIN_ICOS = new HashMap<>();

	// hmm...
	static {
		MIN_ICOS.put("Blaze", new FlexibleItemStack(Items.BLAZE_ROD));
		MIN_ICOS.put("Cave Spider", new FlexibleItemStack(Items.SPIDER_EYE));
		MIN_ICOS.put("Creeper", new FlexibleItemStack(Items.GUNPOWDER));
		MIN_ICOS.put("Enderman", new FlexibleItemStack(Items.ENDER_PEARL));
		MIN_ICOS.put("Ghast", new FlexibleItemStack(Items.GHAST_TEAR));
		MIN_ICOS.put("Magma Cube", new FlexibleItemStack(Items.MAGMA_CREAM));
		MIN_ICOS.put("Skeleton", new FlexibleItemStack(Items.BONE));
		MIN_ICOS.put("Slime", new FlexibleItemStack(Items.SLIME_BALL));
		MIN_ICOS.put("Spider", new FlexibleItemStack(Items.STRING));
		MIN_ICOS.put("Zombie", new FlexibleItemStack(Items.ROTTEN_FLESH));
		MIN_ICOS.put("Cactus", new FlexibleItemStack(Items.CACTUS));
		MIN_ICOS.put("Carrot", new FlexibleItemStack(Items.CARROT));
		MIN_ICOS.put("Chicken", new FlexibleItemStack(Items.CHICKEN));
		MIN_ICOS.put("Cocoa Beans", new FlexibleItemStack(Items.COCOA_BEANS));
		MIN_ICOS.put("Cow", new FlexibleItemStack(Items.BEEF));
		MIN_ICOS.put("Melon", new FlexibleItemStack(Items.MELON_SLICE));
		MIN_ICOS.put("Mushroom", new FlexibleItemStack(Items.RED_MUSHROOM));
		MIN_ICOS.put("Nether Wart", new FlexibleItemStack(Items.NETHER_WART));
		MIN_ICOS.put("Pig", new FlexibleItemStack(Items.PORKCHOP));
		MIN_ICOS.put("Potato", new FlexibleItemStack(Items.POTATO));
		MIN_ICOS.put("Pumpkin", new FlexibleItemStack(Items.PUMPKIN));
		MIN_ICOS.put("Rabbit", new FlexibleItemStack(Items.RABBIT));
		MIN_ICOS.put("Sheep", new FlexibleItemStack(Items.WHITE_WOOL));
		MIN_ICOS.put("Sugar Cane", new FlexibleItemStack(Items.SUGAR_CANE));
		MIN_ICOS.put("Wheat", new FlexibleItemStack(Items.WHEAT));
		MIN_ICOS.put("Clay", new FlexibleItemStack(Items.CLAY));
		MIN_ICOS.put("Fishing", new FlexibleItemStack(Items.FISHING_ROD));
		MIN_ICOS.put("Coal", new FlexibleItemStack(Items.COAL));
		MIN_ICOS.put("Cobblestone", new FlexibleItemStack(Items.COBBLESTONE));
		MIN_ICOS.put("Diamond", new FlexibleItemStack(Items.DIAMOND));
		MIN_ICOS.put("Emerald", new FlexibleItemStack(Items.EMERALD));
		MIN_ICOS.put("End Stone", new FlexibleItemStack(Items.END_STONE));
		MIN_ICOS.put("Glowstone", new FlexibleItemStack(Items.GLOWSTONE_DUST));
		MIN_ICOS.put("Gold", new FlexibleItemStack(Items.GOLD_INGOT));
		MIN_ICOS.put("Gravel", new FlexibleItemStack(Items.GRAVEL));
		MIN_ICOS.put("Hard Stone", new FlexibleItemStack(Items.STONE));
		MIN_ICOS.put("Ice", new FlexibleItemStack(Items.ICE));
		MIN_ICOS.put("Iron", new FlexibleItemStack(Items.IRON_INGOT));
		MIN_ICOS.put("Lapis", new FlexibleItemStack(Items.LAPIS_LAZULI));
		MIN_ICOS.put("Mithril", new FlexibleItemStack(Items.PRISMARINE_CRYSTALS));
		MIN_ICOS.put("Mycelium", new FlexibleItemStack(Items.MYCELIUM));
		MIN_ICOS.put("Obsidian", new FlexibleItemStack(Items.OBSIDIAN));
		MIN_ICOS.put("Quartz", new FlexibleItemStack(Items.QUARTZ));
		MIN_ICOS.put("Red Sand", new FlexibleItemStack(Items.RED_SAND));
		MIN_ICOS.put("Redstone", new FlexibleItemStack(Items.REDSTONE));
		MIN_ICOS.put("Sand", new FlexibleItemStack(Items.SAND));
		MIN_ICOS.put("Snow", new FlexibleItemStack(Items.SNOWBALL));
		MIN_ICOS.put("Inferno", new FlexibleItemStack(Items.BLAZE_SPAWN_EGG));
		MIN_ICOS.put("Revenant", new FlexibleItemStack(Items.ZOMBIE_SPAWN_EGG));
		MIN_ICOS.put("Tarantula", new FlexibleItemStack(Items.SPIDER_SPAWN_EGG));
		MIN_ICOS.put("Vampire", new FlexibleItemStack(Items.REDSTONE));
		MIN_ICOS.put("Voidling", new FlexibleItemStack(Items.ENDERMAN_SPAWN_EGG));
		MIN_ICOS.put("Acacia", new FlexibleItemStack(Items.ACACIA_LOG));
		MIN_ICOS.put("Birch", new FlexibleItemStack(Items.BIRCH_LOG));
		MIN_ICOS.put("Dark Oak", new FlexibleItemStack(Items.DARK_OAK_LOG));
		MIN_ICOS.put("Flower", new FlexibleItemStack(Items.POPPY));
		MIN_ICOS.put("Jungle", new FlexibleItemStack(Items.JUNGLE_LOG));
		MIN_ICOS.put("Oak", new FlexibleItemStack(Items.OAK_LOG));
		MIN_ICOS.put("Spruce", new FlexibleItemStack(Items.SPRUCE_LOG));
	}

	// matches a minion entry
	// group 1: name
	// group 2: level
	// group 3: status
	public static final Pattern MINION_PATTERN = Pattern.compile("^(?<amount>\\d+)x (?<name>.*) (?<level>[XVI]*) \\[(?<status>.*)]");

	public MinionWidget() {
		super("Minions", TITLE, ChatFormatting.DARK_AQUA.getColor());
	}

	@Override
	public void updateContent(List<Component> lines) {
		addComponent(new PlainTextElement(lines.getFirst().copy().append(Component.literal(" minions"))));
		for (int i = 1; i < lines.size(); i++) {
			String string = lines.get(i).getString();
			if (string.toLowerCase(Locale.ENGLISH).startsWith("...")) this.addComponent(new PlainTextElement(lines.get(i).copy().withStyle(ChatFormatting.GRAY)));
			else addMinionComponent(string);
		}
	}

	public void addMinionComponent(String line) {
		Matcher m = MINION_PATTERN.matcher(line);
		if (m.matches()) {

			String min = m.group("name");
			String amount = m.group("amount");
			String lvl = m.group("level");
			String stat = m.group("status");

			MutableComponent mt = Component.literal(amount + "x " + min + " " + lvl).append(Component.literal(": "));

			ChatFormatting format = ChatFormatting.RED;
			if (stat.equals("ACTIVE")) {
				format = ChatFormatting.GREEN;
			} else if (stat.equals("SLOW")) {
				format = ChatFormatting.YELLOW;
			}
			// makes "BLOCKED" also red. in reality, it's some kind of crimson
			mt.append(Component.literal(stat).withStyle(format));

			this.addComponent(Elements.iconTextComponent(MIN_ICOS.get(min), mt));
		}
	}
}
