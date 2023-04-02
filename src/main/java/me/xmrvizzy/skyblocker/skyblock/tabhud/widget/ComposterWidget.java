package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the garden's composter

public class ComposterWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Composter").formatted(Formatting.GREEN,
            Formatting.BOLD);


    public ComposterWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.GREEN.getColorValue());

        Text matter = StrMan.stdEntry(list, 48, "Organic Matter:", Formatting.YELLOW);
        IcoTextComponent mat = new IcoTextComponent(Ico.SAPLING, matter);
        this.addComponent(mat);

        Text fuel = StrMan.stdEntry(list, 49, "Fuel:", Formatting.BLUE);
        IcoTextComponent f = new IcoTextComponent(Ico.FURNACE, fuel);
        this.addComponent(f);

        Text timeLeft = StrMan.stdEntry(list, 50, "Time Left:", Formatting.RED);
        IcoTextComponent time = new IcoTextComponent(Ico.CLOCK, timeLeft);
        this.addComponent(time);

        Text compost = StrMan.stdEntry(list, 51, "Stored Compost:", Formatting.DARK_GREEN);
        IcoTextComponent comp = new IcoTextComponent(Ico.COMPOSTER, compost);
        this.addComponent(comp);

        this.pack();
    }

}
