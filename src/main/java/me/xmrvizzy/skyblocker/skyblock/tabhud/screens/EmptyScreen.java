package me.xmrvizzy.skyblocker.skyblock.tabhud.screens;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EmptyWidget;

import net.minecraft.text.Text;

public class EmptyScreen extends Screen {

    public EmptyScreen(int w, int h, Text footer) {
        super(w, h);
        EmptyWidget ew = new EmptyWidget();
        this.center(ew);
        this.addWidget(ew);
    }

}
