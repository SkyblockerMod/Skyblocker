package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.main;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.QuestWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ReputationWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ServerWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.VolcanoWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class CrimsonIsleScreen extends Screen {

    public CrimsonIsleScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);
        ServerWidget sw = new ServerWidget(ple);
        ReputationWidget rw = new ReputationWidget(ple);
        QuestWidget qw = new QuestWidget(ple);
        VolcanoWidget vw = new VolcanoWidget(ple);
        this.stackWidgetsH(sw, rw, vw);
        this.offCenterL(sw);
        this.offCenterL(rw);
        this.offCenterL(vw);
        this.offCenterR(qw);
        this.centerH(qw);
        this.addWidgets(sw, rw, qw, vw);
    }

}
