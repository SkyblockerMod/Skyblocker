package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDeathWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonDownedWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPuzzleWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonSecretWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonServerWidget;


import net.minecraft.text.Text;

public class DungeonScreen extends Screen {

    public DungeonScreen(int w, int h, Text footer) {
        super(w, h);
        DungeonDownedWidget ddow = new DungeonDownedWidget();
        DungeonDeathWidget ddew = new DungeonDeathWidget();
        DungeonSecretWidget dscw = new DungeonSecretWidget();
        DungeonServerWidget dsrw = new DungeonServerWidget();
        DungeonPuzzleWidget dpuw = new DungeonPuzzleWidget();

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
