package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonBuffWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDeathWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDownedWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPuzzleWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonSecretWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonServerWidget;
import net.minecraft.text.Text;

public class DungeonScreen extends Screen {

    public DungeonScreen(int w, int h, Text footer) {
        super(w, h);

        String f = footer.getString();

        DungeonDownedWidget ddow = new DungeonDownedWidget();
        DungeonDeathWidget ddew = new DungeonDeathWidget();
        DungeonSecretWidget dscw = new DungeonSecretWidget();
        DungeonServerWidget dsrw = new DungeonServerWidget();
        DungeonPuzzleWidget dpuw = new DungeonPuzzleWidget();
        DungeonBuffWidget dbw = new DungeonBuffWidget(f);

        this.offCenterL(ddow);
        this.offCenterL(ddew);
        this.offCenterL(dbw);
        this.offCenterR(dsrw);
        this.offCenterR(dpuw);
        this.offCenterR(dscw);

        this.stackWidgetsH(ddow, ddew, dbw);
        this.stackWidgetsH(dsrw, dpuw, dscw);

        this.addWidgets(ddow, ddew, dscw, dsrw, dpuw, dbw);

    }

}
