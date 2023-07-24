package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;


import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.CameraPositionWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ComposterWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GardenServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GardenVisitorsWidget;
import net.minecraft.text.Text;

public class GardenScreen extends Screen{

    public GardenScreen(int w, int h, Text footer) {
        super(w, h);

        GardenServerWidget gsw = new GardenServerWidget();
        ComposterWidget cw = new ComposterWidget();
        GardenVisitorsWidget vw = new GardenVisitorsWidget();
        CameraPositionWidget cpw = new CameraPositionWidget();

        this.stackWidgetsH(gsw, vw);
        this.stackWidgetsH(cw, cpw);
        this.offCenterL(gsw);
        this.offCenterL(vw);
        this.offCenterR(cw);
        this.offCenterR(cpw);
        this.addWidgets(gsw, cw, vw, cpw);
    }

}
