package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class GenericServerScreen extends Screen {

    public GenericServerScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget(ple);

        this.center(sw);
        this.addWidget(sw);
    }

}
