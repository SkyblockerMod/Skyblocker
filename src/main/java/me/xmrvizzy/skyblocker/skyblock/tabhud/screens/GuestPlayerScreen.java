package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandGuestsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandOwnersWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class GuestPlayerScreen extends Screen{

    public GuestPlayerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);
        IslandGuestsWidget igw = new IslandGuestsWidget(list);
        IslandOwnersWidget iow = new IslandOwnersWidget(list);
        this.centerH(iow);
        this.centerH(igw);
        this.stackWidgetsW(igw, iow);
        this.addWidgets(iow, igw);
    }

}
