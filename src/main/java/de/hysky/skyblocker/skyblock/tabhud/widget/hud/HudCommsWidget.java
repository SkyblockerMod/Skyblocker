package de.hysky.skyblocker.skyblock.tabhud.widget.hud;

import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud.Commission;
import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!

public class HudCommsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

    private List<Commission> commissions;
    private boolean isFancy;

    // disgusting hack to get around text renderer issues.
    // the ctor eventually tries to get the font's height, which doesn't work
    //   when called before the client window is created (roughly).
    // the rebdering god 2 from the fabricord explained that detail, thanks!
    public static final HudCommsWidget INSTANCE = new HudCommsWidget();
    public static final HudCommsWidget INSTANCE_CFG = new HudCommsWidget();

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
                comp = new ProgressComponent(Ico.BOOK, c, p, Colors.pcntToCol(p));
            } else {
                comp = new PlainTextComponent(
                        Text.literal(comm.commission() + ": ").append(
                                Text.literal(comm.progression()).withColor(Colors.pcntToCol(p))
                        )
                );
            }
            this.addComponent(comp);
        }
    }

}
