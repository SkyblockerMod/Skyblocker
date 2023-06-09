package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ParkServerWidget;

import net.minecraft.text.Text;

public class ParkServerScreen extends Screen{

    public ParkServerScreen(int w, int h, Text footer) {
        super(w, h);

        ParkServerWidget sw = new ParkServerWidget();

        this.center(sw);
        this.addWidget(sw);
    }

}
