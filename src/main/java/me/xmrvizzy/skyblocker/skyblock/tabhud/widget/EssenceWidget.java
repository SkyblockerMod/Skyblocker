package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your dungeon essences (dungeon hub only)

public class EssenceWidget extends Widget {

    private Text undead, wither, diamond, gold, dragon, spider, ice, crimson;

    private static final MutableText TITLE = Text.literal("Essences").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public EssenceWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
        wither = StrMan.stdEntry(list, 46, "Wither:", Formatting.DARK_PURPLE);
        spider = StrMan.stdEntry(list, 47, "Spider:", Formatting.DARK_PURPLE);
        undead = StrMan.stdEntry(list, 48, "Undead:", Formatting.DARK_PURPLE);
        dragon = StrMan.stdEntry(list, 49, "Dragon:", Formatting.DARK_PURPLE);
        gold = StrMan.stdEntry(list, 50, "Gold:", Formatting.DARK_PURPLE);
        diamond = StrMan.stdEntry(list, 51, "Diamond:", Formatting.DARK_PURPLE);
        ice = StrMan.stdEntry(list, 52, "Ice:", Formatting.DARK_PURPLE);
        crimson = StrMan.stdEntry(list, 53, "Crimson:", Formatting.DARK_PURPLE);

        TableComponent tc = new TableComponent(2, 4, Formatting.DARK_AQUA.getColorValue());

        tc.addToCell(0, 0, new IcoTextComponent(Ico.WITHER, wither));
        tc.addToCell(0, 1, new IcoTextComponent(Ico.STRING, spider));
        tc.addToCell(0, 2, new IcoTextComponent(Ico.FLESH, undead));
        tc.addToCell(0, 3, new IcoTextComponent(Ico.DRAGON, dragon));
        tc.addToCell(1, 0, new IcoTextComponent(Ico.GOLD, gold));
        tc.addToCell(1, 1, new IcoTextComponent(Ico.DIAMOND, diamond));
        tc.addToCell(1, 2, new IcoTextComponent(Ico.ICE, ice));
        tc.addToCell(1, 3, new IcoTextComponent(Ico.REDSTONE, crimson));
        this.addComponent(tc);
        this.pack();
    }
}
