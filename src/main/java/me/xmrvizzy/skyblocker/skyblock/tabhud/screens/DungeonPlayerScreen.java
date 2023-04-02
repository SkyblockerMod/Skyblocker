package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class DungeonPlayerScreen extends Screen {

    public DungeonPlayerScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        DungeonPlayerWidget dpw1 = new DungeonPlayerWidget(ple, 1);
        DungeonPlayerWidget dpw2 = new DungeonPlayerWidget(ple, 2);
        DungeonPlayerWidget dpw3 = new DungeonPlayerWidget(ple, 3);
        DungeonPlayerWidget dpw4 = new DungeonPlayerWidget(ple, 4);
        DungeonPlayerWidget dpw5 = new DungeonPlayerWidget(ple, 5);

        offCenterL(dpw1);
        offCenterL(dpw2);
        offCenterL(dpw3);
        offCenterR(dpw4);
        offCenterR(dpw5);
        stackWidgetsH(dpw1, dpw2, dpw3);
        stackWidgetsH(dpw4, dpw5);
        addWidgets(dpw1, dpw2, dpw3, dpw4, dpw5);
    }

}
