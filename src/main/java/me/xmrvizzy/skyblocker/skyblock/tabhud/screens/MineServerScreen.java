package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.CommsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ForgeWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.PowderWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class MineServerScreen extends Screen{

    public MineServerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);
        ServerWidget sw = new ServerWidget(list);
        PowderWidget pw = new PowderWidget(list);
        CommsWidget cw = new CommsWidget(list);
        ForgeWidget fw = new ForgeWidget(list);
        stackWidgetsH(sw, pw, cw);
        centerH(fw);
        offCenterL(sw);
        offCenterL(pw);
        offCenterL(cw);
        offCenterR(fw);
        this.addWidgets(fw, cw, pw, sw);
    }

}
