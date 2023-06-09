package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;


import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EssenceWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.text.Text;

public class DungeonHubScreen extends Screen{

    public DungeonHubScreen(int w, int h, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget();
        EssenceWidget ew = new EssenceWidget();

        this.centerW(sw);
        this.centerW(ew);
        this.stackWidgetsH(sw, ew);
        this.addWidget(ew);
        this.addWidget(sw);
    }

}
