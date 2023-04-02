package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.TrapperWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class FarmingServerScreen extends Screen{

    public FarmingServerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);
        ServerWidget sw = new ServerWidget(list);
        TrapperWidget tw = new TrapperWidget(list);
        centerW(sw);
        centerW(tw);
        stackWidgetsH(sw, tw);
        this.addWidgets(tw, sw);
    }

}
