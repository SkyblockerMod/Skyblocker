package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GuestServerWidget;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class GuestServerScreen extends Screen{

    public GuestServerScreen(int w, int h, List<PlayerListEntry> list, Text footer) {
        super(w, h);

        GuestServerWidget gsw = new GuestServerWidget(list);

        this.center(gsw);
        this.addWidget(gsw);
    }

}
