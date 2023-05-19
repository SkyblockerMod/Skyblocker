package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.TrapperWidget;


import net.minecraft.text.Text;

public class FarmingServerScreen extends Screen{

    public FarmingServerScreen(int w, int h, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget();
        TrapperWidget tw = new TrapperWidget();

        this.centerW(sw);
        this.centerW(tw);
        this.stackWidgetsH(sw, tw);
        this.addWidgets(tw, sw);
    }

}
