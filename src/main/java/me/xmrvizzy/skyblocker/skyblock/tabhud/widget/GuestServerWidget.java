package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;


import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the private island you're visiting

public class GuestServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Island Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public GuestServerWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41);
        this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42);
        this.addSimpleIcoText(Ico.SIGN, "Owner:", Formatting.GREEN, 43);
        this.addSimpleIcoText(Ico.SIGN, "Status:", Formatting.BLUE, 44);

    }

}
