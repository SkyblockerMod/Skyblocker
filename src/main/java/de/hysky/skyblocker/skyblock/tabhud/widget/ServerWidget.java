package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.skyblock.tabhud.util.Ico;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about "generic" servers.
// a server is "generic", when only name, server ID and gems are shown
// in the third column of the tab HUD

public class ServerWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public ServerWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        this.addSimpleIcoText(Ico.MAP, "Area:", Formatting.DARK_AQUA, 41);
        this.addSimpleIcoText(Ico.NTAG, "Server ID:", Formatting.GRAY, 42);
        this.addSimpleIcoText(Ico.EMERALD, "Gems:", Formatting.GREEN, 43);
    }

}
