package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandGuestsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandSelfWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class HomePlayerScreen extends Screen {

    public HomePlayerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);
        IslandSelfWidget isw = new IslandSelfWidget(list);
        IslandGuestsWidget igw = new IslandGuestsWidget(list);
        this.centerH(isw);
        this.centerH(igw);
        this.stackWidgetsW(isw, igw);
        this.addWidgets(isw, igw);
    }
}
