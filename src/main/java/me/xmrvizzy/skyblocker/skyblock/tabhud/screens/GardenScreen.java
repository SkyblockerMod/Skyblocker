package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ComposterWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GardenServerWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class GardenScreen extends Screen{

    public GardenScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        GardenServerWidget gsw = new GardenServerWidget(ple);
        ComposterWidget cw = new ComposterWidget(ple);

        this.stackWidgetsH(gsw, cw);
        this.centerW(gsw);
        this.centerW(cw);
        this.addWidgets(gsw, cw);
    }

}
