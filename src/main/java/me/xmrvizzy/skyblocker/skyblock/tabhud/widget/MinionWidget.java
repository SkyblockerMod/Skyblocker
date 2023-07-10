package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about minions placed on the home island

public class MinionWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Minions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    private static final HashMap<String, ItemStack> MIN_ICOS = new HashMap<>();

    // hmm...
    static {
        MIN_ICOS.put("Blaze", new ItemStack(Items.BLAZE_ROD));
        MIN_ICOS.put("Cave Spider", new ItemStack(Items.SPIDER_EYE));
        MIN_ICOS.put("Creeper", new ItemStack(Items.GUNPOWDER));
        MIN_ICOS.put("Enderman", new ItemStack(Items.ENDER_PEARL));
        MIN_ICOS.put("Ghast", new ItemStack(Items.GHAST_TEAR));
        MIN_ICOS.put("Magma Cube", new ItemStack(Items.MAGMA_CREAM));
        MIN_ICOS.put("Skeleton", new ItemStack(Items.BONE));
        MIN_ICOS.put("Slime", new ItemStack(Items.SLIME_BALL));
        MIN_ICOS.put("Spider", new ItemStack(Items.STRING));
        MIN_ICOS.put("Zombie", new ItemStack(Items.ROTTEN_FLESH));
        MIN_ICOS.put("Cactus", new ItemStack(Items.CACTUS));
        MIN_ICOS.put("Carrot", new ItemStack(Items.CARROT));
        MIN_ICOS.put("Chicken", new ItemStack(Items.CHICKEN));
        MIN_ICOS.put("Cocoa Beans", new ItemStack(Items.COCOA_BEANS));
        MIN_ICOS.put("Cow", new ItemStack(Items.BEEF));
        MIN_ICOS.put("Melon", new ItemStack(Items.MELON_SLICE));
        MIN_ICOS.put("Mushroom", new ItemStack(Items.RED_MUSHROOM));
        MIN_ICOS.put("Nether Wart", new ItemStack(Items.NETHER_WART));
        MIN_ICOS.put("Pig", new ItemStack(Items.PORKCHOP));
        MIN_ICOS.put("Potato", new ItemStack(Items.POTATO));
        MIN_ICOS.put("Pumpkin", new ItemStack(Items.PUMPKIN));
        MIN_ICOS.put("Rabbit", new ItemStack(Items.RABBIT));
        MIN_ICOS.put("Sheep", new ItemStack(Items.WHITE_WOOL));
        MIN_ICOS.put("Sugar Cane", new ItemStack(Items.SUGAR_CANE));
        MIN_ICOS.put("Wheat", new ItemStack(Items.WHEAT));
        MIN_ICOS.put("Clay", new ItemStack(Items.CLAY));
        MIN_ICOS.put("Fishing", new ItemStack(Items.FISHING_ROD));
        MIN_ICOS.put("Coal", new ItemStack(Items.COAL));
        MIN_ICOS.put("Cobblestone", new ItemStack(Items.COBBLESTONE));
        MIN_ICOS.put("Diamond", new ItemStack(Items.DIAMOND));
        MIN_ICOS.put("Emerald", new ItemStack(Items.EMERALD));
        MIN_ICOS.put("End Stone", new ItemStack(Items.END_STONE));
        MIN_ICOS.put("Glowstone", new ItemStack(Items.GLOWSTONE_DUST));
        MIN_ICOS.put("Gold", new ItemStack(Items.GOLD_INGOT));
        MIN_ICOS.put("Gravel", new ItemStack(Items.GRAVEL));
        MIN_ICOS.put("Hard Stone", new ItemStack(Items.STONE));
        MIN_ICOS.put("Ice", new ItemStack(Items.ICE));
        MIN_ICOS.put("Iron", new ItemStack(Items.IRON_INGOT));
        MIN_ICOS.put("Lapis", new ItemStack(Items.LAPIS_LAZULI));
        MIN_ICOS.put("Mithril", new ItemStack(Items.PRISMARINE_CRYSTALS));
        MIN_ICOS.put("Mycelium", new ItemStack(Items.MYCELIUM));
        MIN_ICOS.put("Obsidian", new ItemStack(Items.OBSIDIAN));
        MIN_ICOS.put("Quartz", new ItemStack(Items.QUARTZ));
        MIN_ICOS.put("Red Sand", new ItemStack(Items.RED_SAND));
        MIN_ICOS.put("Redstone", new ItemStack(Items.REDSTONE));
        MIN_ICOS.put("Sand", new ItemStack(Items.SAND));
        MIN_ICOS.put("Snow", new ItemStack(Items.SNOWBALL));
        MIN_ICOS.put("Inferno", new ItemStack(Items.BLAZE_SPAWN_EGG));
        MIN_ICOS.put("Revenant", new ItemStack(Items.ZOMBIE_SPAWN_EGG));
        MIN_ICOS.put("Tarantula", new ItemStack(Items.SPIDER_SPAWN_EGG));
        MIN_ICOS.put("Vampire", new ItemStack(Items.REDSTONE));
        MIN_ICOS.put("Voidling", new ItemStack(Items.ENDERMAN_SPAWN_EGG));
        MIN_ICOS.put("Acacia", new ItemStack(Items.ACACIA_LOG));
        MIN_ICOS.put("Birch", new ItemStack(Items.BIRCH_LOG));
        MIN_ICOS.put("Dark Oak", new ItemStack(Items.DARK_OAK_LOG));
        MIN_ICOS.put("Flower", new ItemStack(Items.POPPY));
        MIN_ICOS.put("Jungle", new ItemStack(Items.JUNGLE_LOG));
        MIN_ICOS.put("Oak", new ItemStack(Items.OAK_LOG));
        MIN_ICOS.put("Spruce", new ItemStack(Items.SPRUCE_LOG));
    }

    // matches a minion entry
    // group 1: name
    // group 2: level
    // group 3: status
    public static final Pattern MINION_PATTERN = Pattern.compile("(?<name>.*) (?<level>[XVI]*) \\[(?<status>.*)\\]");

    public MinionWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        for (int i = 48; i < 59; i++) {
            Matcher m = PlayerListMgr.regexAt(i, MINION_PATTERN);
            if (m != null) {

                String min = m.group("name");
                String lvl = m.group("level");
                String stat = m.group("status");

                MutableText mt = Text.literal(min + " " + lvl).append(Text.literal(": "));

                Formatting format = Formatting.RED;
                if (stat.equals("ACTIVE")) {
                    format = Formatting.GREEN;
                } else if (stat.equals("SLOW")) {
                    format = Formatting.YELLOW;
                }
                // makes "BLOCKED" also red. in reality, it's some kind of crimson
                mt.append(Text.literal(stat).formatted(format));

                IcoTextComponent itc = new IcoTextComponent(MIN_ICOS.get(min), mt);
                this.addComponent(itc);

            } else {
                break;
            }
        }

        // if more minions are placed than the tab menu can display,
        // a "And X more..." text is shown
        // look for that and add it to the widget
        String more = PlayerListMgr.strAt(59);
        if (more != null) {
            this.addComponent(new PlainTextComponent(Text.of(more)));
        }
        this.pack();
    }

}
