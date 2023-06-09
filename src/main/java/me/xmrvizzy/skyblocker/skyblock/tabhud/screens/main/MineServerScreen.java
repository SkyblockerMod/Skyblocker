package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.CommsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ForgeWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.PowderWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.text.Text;

public class MineServerScreen extends Screen {

    public MineServerScreen(int w, int h, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget();
        PowderWidget pw = new PowderWidget();
        CommsWidget cw = new CommsWidget();
        ForgeWidget fw = new ForgeWidget();

        this.stackWidgetsH(sw, cw);
        this.stackWidgetsH(fw, pw);
        this.offCenterL(sw);
        this.offCenterL(cw);
        this.offCenterR(pw);
        this.offCenterR(fw);
        this.addWidgets(fw, cw, pw, sw);
    }

}
