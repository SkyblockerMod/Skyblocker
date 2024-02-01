package de.hysky.skyblocker.skyblock.tabhud.widget.hud;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)
// USE ONLY WITH THE DWARVEN HUD!

public class HudPowderWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);


    // disgusting hack to get around text renderer issues.
    // the ctor eventually tries to get the font's height, which doesn't work
    //   when called before the client window is created (roughly).
    // the rebdering god 2 from the fabricord explained that detail, thanks!
    //coppied from the HodCommsWidget to be used in the same place
    public static final HudPowderWidget INSTANCE = new HudPowderWidget();
    public static final HudPowderWidget INSTANCE_CFG = new HudPowderWidget();

    // another repulsive hack to make this widget-like hud element work with the new widget class
    // DON'T USE WITH THE WIDGET SYSTEM, ONLY USE FOR DWARVENHUD!
    public HudPowderWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }


    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MITHRIL, "Mithril:", Formatting.AQUA, 46);
        this.addSimpleIcoText(Ico.AMETHYST_SHARD, "Gemstone:", Formatting.DARK_PURPLE, 47);
    }

}
