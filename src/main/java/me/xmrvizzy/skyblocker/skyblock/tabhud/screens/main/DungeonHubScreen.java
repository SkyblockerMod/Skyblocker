package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EssenceWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class DungeonHubScreen extends Screen{

    public DungeonHubScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget(list);
        EssenceWidget ew = new EssenceWidget(list);

        this.centerW(sw);
        this.centerW(ew);
        this.stackWidgetsH(sw, ew);
        this.addWidget(ew);
        this.addWidget(sw);
    }

}
