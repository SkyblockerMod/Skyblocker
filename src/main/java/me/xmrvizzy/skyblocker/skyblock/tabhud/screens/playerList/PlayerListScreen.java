package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList;


import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.PlayerListWidget;

import net.minecraft.text.Text;

public class PlayerListScreen extends Screen {

    public PlayerListScreen(int w, int h, Text footer) {
        super(w, h);

        PlayerListWidget plw = new PlayerListWidget();

        this.center(plw);
        this.addWidget(plw);
    }

}
