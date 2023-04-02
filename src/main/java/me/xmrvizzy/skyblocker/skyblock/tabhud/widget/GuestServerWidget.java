package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the private island you're visiting

public class GuestServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Island Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public GuestServerWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
        Text areaName = StrMan.stdEntry(list, 41, "Area:", Formatting.DARK_AQUA);
        IcoTextComponent area = new IcoTextComponent(Ico.MAP, areaName);
        this.addComponent(area);

        Text serverName = StrMan.stdEntry(list, 42, "Server ID:", Formatting.GRAY);
        IcoTextComponent server = new IcoTextComponent(Ico.NTAG, serverName);
        this.addComponent(server);

        Text owner = StrMan.stdEntry(list, 43, "Owner:", Formatting.GREEN);
        IcoTextComponent own = new IcoTextComponent(Ico.SIGN, owner);
        this.addComponent(own);

        Text status = StrMan.stdEntry(list, 44, "Status:", Formatting.BLUE);
        IcoTextComponent stat = new IcoTextComponent(Ico.SIGN, status);
        this.addComponent(stat);
        this.pack();
    }

}
