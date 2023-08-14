package me.xmrvizzy.skyblocker.skyblock.tabhud.widget.hud;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.Widget;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.Component;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!

public class HudCommsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    private List<Commission> commissions;
    private boolean isFancy;

    // disgusting hack to get around text renderer issues.
    // the ctor eventually tries to get the font's height, which doesn't work
    //   when called before the client window is created (roughly).
    // the rebdering god 2 from the fabricord explained that detail, thanks!
    public static HudCommsWidget INSTANCE = new HudCommsWidget();
    public static HudCommsWidget INSTANCE_CFG = new HudCommsWidget();

    // another repulsive hack to make this widget-like hud element work with the new widget class
    // DON'T USE WITH THE WIDGET SYSTEM, ONLY USE FOR DWARVENHUD!
    public HudCommsWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    public void updateData(List<Commission> commissions, boolean isFancy) {
        this.commissions = commissions;
        this.isFancy = isFancy;
    }

    @Override
    public void updateContent() {
        for (Commission comm : commissions) {

            Text c = Text.literal(comm.commission());

            float p = 100f;
            if (!comm.progression().contains("DONE")) {
                p = Float.parseFloat(comm.progression().substring(0, comm.progression().length() - 1));
            }

            Component comp;
            if (isFancy) {
                comp = new ProgressComponent(Ico.BOOK, c, p, pcntToCol(p));
            } else {
                comp = new PlainTextComponent(
                        Text.literal(comm.commission() + ": ")
                                .append(Text.literal(comm.progression()).formatted(Formatting.GREEN)));
            }
            this.addComponent(comp);
        }
    }

    private int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb(pcnt / 300f, 0.9f, 0.9f);
    }

}
