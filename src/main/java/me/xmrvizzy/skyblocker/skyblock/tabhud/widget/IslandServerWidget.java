package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about your home island

public class IslandServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Island Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public IslandServerWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        Text areaName = StrMan.stdEntry(list, 41, "Area:", Formatting.DARK_AQUA);
        IcoTextComponent area = new IcoTextComponent(Ico.MAP, areaName);
        this.addComponent(area);

        Text serverName = StrMan.stdEntry(list, 42, "Server ID:", Formatting.GRAY);
        IcoTextComponent server = new IcoTextComponent(Ico.NTAG, serverName);
        this.addComponent(server);

        Text crystals = StrMan.stdEntry(list, 43, "Crystals:", Formatting.DARK_PURPLE);
        IcoTextComponent crys = new IcoTextComponent(Ico.EMERALD, crystals);
        this.addComponent(crys);

        Text stash = StrMan.stdEntry(list, 44, "Stash:", Formatting.GREEN);
        IcoTextComponent st = new IcoTextComponent(Ico.CHEST, stash);
        this.addComponent(st);

        Text minions = StrMan.stdEntry(list, 45, "Minions:", Formatting.BLUE);
        IcoTextComponent min = new IcoTextComponent(Ico.COMMAND, minions);
        this.addComponent(min);
        this.pack();

    }

}
