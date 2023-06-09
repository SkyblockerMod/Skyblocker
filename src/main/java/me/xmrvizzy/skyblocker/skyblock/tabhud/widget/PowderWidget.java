package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;


import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows how much mithril and gemstone powder you have
// (dwarven mines and crystal hollows)

public class PowderWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Powders").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public PowderWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        this.addSimpleIcoText(Ico.MITHRIL, "Mithril:", Formatting.AQUA, 46);
        this.addSimpleIcoText(Ico.EMERALD, "Gemstone:", Formatting.DARK_PURPLE, 47);

        this.pack();

    }

}
