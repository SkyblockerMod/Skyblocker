package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GuestServerWidget;


import net.minecraft.text.Text;

public class GuestServerScreen extends Screen{

    public GuestServerScreen(int w, int h, Text footer) {
        super(w, h);

        GuestServerWidget gsw = new GuestServerWidget();

        this.center(gsw);
        this.addWidget(gsw);
    }

}
