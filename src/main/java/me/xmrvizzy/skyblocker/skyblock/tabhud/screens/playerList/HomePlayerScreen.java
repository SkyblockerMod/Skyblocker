package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.playerList;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandGuestsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandSelfWidget;


import net.minecraft.text.Text;

public class HomePlayerScreen extends Screen {

    public HomePlayerScreen(int w, int h, Text footer) {
        super(w, h);

        IslandSelfWidget isw = new IslandSelfWidget();
        IslandGuestsWidget igw = new IslandGuestsWidget();

        this.centerH(isw);
        this.centerH(igw);
        this.stackWidgetsW(isw, igw);
        this.addWidgets(isw, igw);
    }
}
