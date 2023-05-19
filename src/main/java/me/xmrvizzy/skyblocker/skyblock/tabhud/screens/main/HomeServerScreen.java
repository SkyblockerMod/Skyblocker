package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.IslandServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.MinionWidget;


import net.minecraft.text.Text;

public class HomeServerScreen extends Screen {

    public HomeServerScreen(int w, int h, Text footer) {
        super(w, h);

        IslandServerWidget isw = new IslandServerWidget();
        MinionWidget mw = new MinionWidget();

        this.centerH(isw);
        this.centerH(mw);
        this.stackWidgetsW(isw, mw);
        this.addWidgets(isw, mw);
    }

}
