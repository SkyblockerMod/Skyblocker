package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.DungeonPlayerWidget;


import net.minecraft.text.Text;

public class DungeonPlayerScreen extends Screen {

    public DungeonPlayerScreen(int w, int h, Text footer) {
        super(w, h);

        DungeonPlayerWidget dpw1 = new DungeonPlayerWidget(1);
        DungeonPlayerWidget dpw2 = new DungeonPlayerWidget(2);
        DungeonPlayerWidget dpw3 = new DungeonPlayerWidget(3);
        DungeonPlayerWidget dpw4 = new DungeonPlayerWidget(4);
        DungeonPlayerWidget dpw5 = new DungeonPlayerWidget(5);

        this.offCenterL(dpw1);
        this.offCenterL(dpw2);
        this.offCenterL(dpw3);
        this.offCenterR(dpw4);
        this.offCenterR(dpw5);
        this.stackWidgetsH(dpw1, dpw2, dpw3);
        this.stackWidgetsH(dpw4, dpw5);
        this.addWidgets(dpw1, dpw2, dpw3, dpw4, dpw5);
    }

}
