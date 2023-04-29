package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;
import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

// this widget shows the volcano status

public class VolcanoWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Volcano Status").formatted(Formatting.AQUA,
            Formatting.BOLD);

    private static final HashMap<String, Pair<ItemStack, Formatting>> BOOM_TYPE = new HashMap<>();

    static {
        BOOM_TYPE.put("INACTIVE",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.BARRIER), Formatting.DARK_GRAY));
        BOOM_TYPE.put("CHILL",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.ICE), Formatting.AQUA));
        BOOM_TYPE.put("LOW",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.FLINT_AND_STEEL), Formatting.GRAY));
        BOOM_TYPE.put("DISRUPTIVE",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.CAMPFIRE), Formatting.WHITE));
        BOOM_TYPE.put("MEDIUM",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.LAVA_BUCKET), Formatting.YELLOW));
        BOOM_TYPE.put("HIGH",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.FIRE_CHARGE), Formatting.GOLD));
        BOOM_TYPE.put("EXPLOSIVE",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.TNT), Formatting.RED));
        BOOM_TYPE.put("CATACLYSMIC",
                new Pair<ItemStack, Formatting>(new ItemStack(Items.SKELETON_SKULL), Formatting.DARK_RED));
    }

    public VolcanoWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.AQUA.getColorValue());

        String s = StrMan.strAt(list, 58).trim();
        Pair<ItemStack, Formatting> p = BOOM_TYPE.get(s);
        IcoTextComponent itc = new IcoTextComponent(p.getLeft(), Text.literal(s).formatted(p.getRight()));
        this.addComponent(itc);
        this.pack();

    }

}
