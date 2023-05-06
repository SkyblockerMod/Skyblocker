package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.genericInfo;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.CookieWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EffectWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EventWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.GardenSkillsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ProfileWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.UpgradeWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class GardenInfoScreen extends Screen {

    public GardenInfoScreen(int w, int h, List<PlayerListEntry> ple, Text footer) {
        super(w, h);

        String f = footer.getString();

        GardenSkillsWidget gsw = new GardenSkillsWidget(ple);
        EventWidget evw = new EventWidget(ple, true);
        UpgradeWidget uw = new UpgradeWidget(f);

        ProfileWidget pw = new ProfileWidget(ple);
        EffectWidget efw = new EffectWidget(f);

        JacobsContestWidget jcw = new JacobsContestWidget(ple);
        CookieWidget cw = new CookieWidget(f);

        // layout code incoming
        this.stackWidgetsH(gsw, evw, uw);
        this.stackWidgetsH(pw, efw);
        this.stackWidgetsH(jcw, cw);

        this.centerW(gsw);
        this.centerW(evw);
        this.centerW(uw);

        this.collideAgainstL(pw, gsw, evw, uw);
        this.collideAgainstL(efw, gsw, evw, uw);

        this.collideAgainstR(jcw, gsw, evw, uw);
        this.collideAgainstR(cw, gsw, evw, uw);

        this.addWidgets(gsw, evw, uw, pw, efw, jcw, cw);
    }

}
