package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.PlayerListWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class PlayerListScreen extends Screen {

    public PlayerListScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        PlayerListWidget plw = new PlayerListWidget(ple);
        this.center(plw);
        this.addWidget(plw);
    }

}
