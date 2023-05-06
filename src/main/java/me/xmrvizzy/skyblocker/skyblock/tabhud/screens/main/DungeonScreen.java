package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDeathWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDownedWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPuzzleWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonSecretWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class DungeonScreen extends Screen {

    public DungeonScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        DungeonDownedWidget ddow = new DungeonDownedWidget(ple);
        DungeonDeathWidget ddew = new DungeonDeathWidget(ple);
        DungeonSecretWidget dscw = new DungeonSecretWidget(ple);
        DungeonServerWidget dsrw = new DungeonServerWidget(ple);
        DungeonPuzzleWidget dpuw = new DungeonPuzzleWidget(ple);

        this.offCenterL(ddow);
        this.offCenterL(ddew);
        this.offCenterL(dscw);
        this.offCenterR(dsrw);
        this.offCenterR(dpuw);

        this.stackWidgetsH(ddow, ddew, dscw);
        this.stackWidgetsH(dsrw, dpuw);

        this.addWidgets(ddow, ddew, dscw, dsrw, dpuw);

    }

}
