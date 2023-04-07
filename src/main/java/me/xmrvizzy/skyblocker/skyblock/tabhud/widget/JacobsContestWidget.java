package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;
import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JacobsContestWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Jacob's Contest").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    private static final HashMap<String, ItemStack> FARM_DATA = new HashMap<>();

    // again, there HAS to be a better way to do this
    static {
        FARM_DATA.put("Wheat", new ItemStack(Items.WHEAT));
        FARM_DATA.put("Sugar Cane", new ItemStack(Items.SUGAR_CANE));
        FARM_DATA.put("Carrot", new ItemStack(Items.CARROT));
        FARM_DATA.put("Potato", new ItemStack(Items.POTATO));
        FARM_DATA.put("Melon", new ItemStack(Items.MELON_SLICE));
        FARM_DATA.put("Pumpkin", new ItemStack(Items.PUMPKIN));
        FARM_DATA.put("Cocoa Beans", new ItemStack(Items.COCOA_BEANS));
        FARM_DATA.put("Nether Wart", new ItemStack(Items.NETHER_WART));
        FARM_DATA.put("Cactus", new ItemStack(Items.CACTUS));
        FARM_DATA.put("Mushroom", new ItemStack(Items.RED_MUSHROOM));
    }

    public JacobsContestWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        Text time = StrMan.stdEntry(list, 76, "Starts in:", Formatting.GOLD);
        IcoTextComponent t = new IcoTextComponent(Ico.CLOCK, time);
        this.addComponent(t);

        TableComponent tc = new TableComponent(1, 3, Formatting.YELLOW  .getColorValue());

        for (int i = 77; i < 80; i++) {
            String item = StrMan.strAt(list, i).trim();
            tc.addToCell(0, i - 77, new IcoTextComponent(FARM_DATA.get(item), Text.of(item)));
        }
        this.addComponent(tc);

        this.pack();
    }

}
