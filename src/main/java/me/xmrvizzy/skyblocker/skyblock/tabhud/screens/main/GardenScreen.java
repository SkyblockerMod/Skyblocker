package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;


import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ComposterWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GardenServerWidget;
import net.minecraft.text.Text;

public class GardenScreen extends Screen{

    public GardenScreen(int w, int h, Text footer) {
        super(w, h);

        GardenServerWidget gsw = new GardenServerWidget();
        ComposterWidget cw = new ComposterWidget();

        this.stackWidgetsH(gsw, cw);
        this.centerW(gsw);
        this.centerW(cw);
        this.addWidgets(gsw, cw);
    }

}
