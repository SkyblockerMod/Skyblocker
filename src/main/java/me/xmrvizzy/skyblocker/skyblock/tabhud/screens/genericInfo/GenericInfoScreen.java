package me.xmrvizzy.skyblocker.skyblock.tabhud.screens.genericInfo;



import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.CookieWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EffectWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ElectionWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.EventWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.ProfileWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.SkillsWidget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.UpgradeWidget;


import net.minecraft.text.Text;

public class GenericInfoScreen extends Screen {

    public GenericInfoScreen(int w, int h, Text footer) {
        super(w, h);

        String f = footer.getString();

        SkillsWidget sw = new SkillsWidget();
        EventWidget evw = new EventWidget(false);
        UpgradeWidget uw = new UpgradeWidget(f);

        ProfileWidget pw = new ProfileWidget();
        EffectWidget efw = new EffectWidget(f);

        ElectionWidget elw = new ElectionWidget();
        CookieWidget cw = new CookieWidget(f);

        this.stackWidgetsH(sw, evw, uw);
        this.stackWidgetsH(pw, efw);
        this.stackWidgetsH(elw, cw);

        this.centerW(sw);
        this.centerW(evw);
        this.centerW(uw);

        this.collideAgainstL(pw, sw, evw, uw);
        this.collideAgainstL(efw, sw, evw, uw);

        this.collideAgainstR(elw, sw, evw, uw);
        this.collideAgainstR(cw, sw, evw, uw);

        this.addWidgets(sw, evw, uw, pw, efw, elw, cw);
    }

}
