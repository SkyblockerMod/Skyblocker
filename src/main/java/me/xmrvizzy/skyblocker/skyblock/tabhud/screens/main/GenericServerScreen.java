package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;

import net.minecraft.text.Text;

public class GenericServerScreen extends Screen {

    public GenericServerScreen(int w, int h, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget();

        this.center(sw);
        this.addWidget(sw);
    }

}
