package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the current jacob's contest (garden only)

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

    public JacobsContestWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.CLOCK, "Starts in:", Formatting.GOLD, 76);

        TableComponent tc = new TableComponent(1, 3, Formatting.YELLOW  .getColorValue());

        for (int i = 77; i < 80; i++) {
            String item = PlayerListMgr.strAt(i);
            IcoTextComponent itc;
            if (item == null) {
                itc = new IcoTextComponent();
            } else {
                itc = new IcoTextComponent(FARM_DATA.get(item), Text.of(item));
            }
            tc.addToCell(0, i - 77, itc);
        }
        this.addComponent(tc);

    }

}
