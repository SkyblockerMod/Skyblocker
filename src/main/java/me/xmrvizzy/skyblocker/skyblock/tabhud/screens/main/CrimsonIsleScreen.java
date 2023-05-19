package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.QuestWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ReputationWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.VolcanoWidget;

import net.minecraft.text.Text;

public class CrimsonIsleScreen extends Screen {

    public CrimsonIsleScreen(int w, int h, Text footer) {
        super(w, h);

        ServerWidget sw = new ServerWidget();
        ReputationWidget rw = new ReputationWidget();
        QuestWidget qw = new QuestWidget();
        VolcanoWidget vw = new VolcanoWidget();

        this.stackWidgetsH(sw, rw);
        this.stackWidgetsH(qw, vw);
        this.offCenterL(sw);
        this.offCenterL(rw);
        this.offCenterR(vw);
        this.offCenterR(qw);
        this.addWidgets(sw, rw, qw, vw);
    }

}
