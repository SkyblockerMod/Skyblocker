package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about the garden's composter

public class ComposterWidget extends HudWidget {

    private static final MutableText TITLE = Text.literal("Composter").formatted(Formatting.GREEN,
            Formatting.BOLD);

    public ComposterWidget() {
        super(TITLE, Formatting.GREEN.getColorValue());
    }

    @Override
    public void updateContent() {
        int offset = (PlayerListMgr.strAt(46) != null) ? 1 : 0;

        this.addSimpleIcoText(Ico.SAPLING, "Organic Matter:", Formatting.YELLOW, 48 + offset);
        this.addSimpleIcoText(Ico.FURNACE, "Fuel:", Formatting.BLUE, 49 + offset);
        this.addSimpleIcoText(Ico.CLOCK, "Time Left:", Formatting.RED, 50 + offset);
        this.addSimpleIcoText(Ico.COMPOSTER, "Stored Compost:", Formatting.DARK_GREEN, 51 + offset);
    }
}
