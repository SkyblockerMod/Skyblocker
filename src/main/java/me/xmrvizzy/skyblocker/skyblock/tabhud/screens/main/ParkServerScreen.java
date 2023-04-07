package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ParkServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class ParkServerScreen extends Screen{

    public ParkServerScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        ParkServerWidget sw = new ParkServerWidget(ple);
        this.center(sw);
        this.addWidget(sw);
    }

}
