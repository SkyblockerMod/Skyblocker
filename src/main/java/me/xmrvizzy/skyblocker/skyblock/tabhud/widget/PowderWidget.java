package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)

public class PowderWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public PowderWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        Text amtMith = StrMan.stdEntry(list, 46, "Mithril:", Formatting.AQUA);
        IcoTextComponent mith = new IcoTextComponent(Ico.MITHRIL, amtMith);
        this.addComponent(mith);

        Text amtGem = StrMan.stdEntry(list, 47, "Gemstone:", Formatting.DARK_PURPLE);
        IcoTextComponent gem = new IcoTextComponent(Ico.EMERALD, amtGem);
        this.addComponent(gem);
        this.pack();

    }

}
