package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandGuestsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandOwnersWidget;


import net.minecraft.text.Text;

public class GuestPlayerScreen extends Screen{

    public GuestPlayerScreen(int w, int h, Text footer) {
        super(w, h);

        IslandGuestsWidget igw = new IslandGuestsWidget();
        IslandOwnersWidget iow = new IslandOwnersWidget();

        this.centerH(iow);
        this.centerH(igw);
        this.stackWidgetsW(igw, iow);
        this.addWidgets(iow, igw);
    }

}
