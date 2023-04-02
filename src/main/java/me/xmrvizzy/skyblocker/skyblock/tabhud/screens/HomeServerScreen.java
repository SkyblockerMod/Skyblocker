package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.MinionWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class HomeServerScreen extends Screen {

    public HomeServerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);

        IslandServerWidget isw = new IslandServerWidget(list);
        MinionWidget mw = new MinionWidget(list);
        this.centerH(isw);
        this.centerH(mw);
        this.stackWidgetsW(isw, mw);
        this.addWidgets(isw, mw);
    }

}
