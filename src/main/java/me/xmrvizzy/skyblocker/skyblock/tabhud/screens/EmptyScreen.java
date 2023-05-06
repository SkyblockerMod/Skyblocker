package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EmptyWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class EmptyScreen extends Screen {

    public EmptyScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        EmptyWidget ew = new EmptyWidget(ple);
        this.center(ew);
        this.addWidget(ew);
    }

}
