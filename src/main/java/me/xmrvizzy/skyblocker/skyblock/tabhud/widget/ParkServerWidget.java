package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the park server

public class ParkServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public ParkServerWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        Text areaName = StrMan.stdEntry(list, 41, "Area:", Formatting.DARK_AQUA);
        IcoTextComponent area = new IcoTextComponent(Ico.MAP, areaName);
        this.addComponent(area);

        Text serverName = StrMan.stdEntry(list, 42, "Server ID:", Formatting.GRAY);
        IcoTextComponent server = new IcoTextComponent(Ico.NTAG, serverName);
        this.addComponent(server);

        Text amtGems = StrMan.stdEntry(list, 43, "Gems:", Formatting.GREEN);
        IcoTextComponent gems = new IcoTextComponent(Ico.EMERALD, amtGems);
        this.addComponent(gems);

        Text rain = StrMan.stdEntry(list, 44, "Rain:", Formatting.BLUE);
        IcoTextComponent ra = new IcoTextComponent(Ico.WATER, rain);
        this.addComponent(ra);
        this.pack();
    }

}
