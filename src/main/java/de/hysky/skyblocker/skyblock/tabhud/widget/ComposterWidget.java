package de.hysky.skyblocker.skyblock.tabhud.widget;


import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

// this widget shows info about the garden's composter

public class ComposterWidget extends TabHudWidget {

    private static final MutableText TITLE = Text.literal("Composter").formatted(Formatting.GREEN,
            Formatting.BOLD);

    public ComposterWidget() {
        super("Composter", TITLE, Formatting.GREEN.getColorValue());
    }

    @Override
    public void updateContent(List<Text> lines) {
        int offset = (PlayerListMgr.strAt(46) != null) ? 1 : 0;

        for (Text line : lines) {
            switch (line.getString().toLowerCase()) {
                case String s when s.contains("organic") -> this.addComponent(new IcoTextComponent(Ico.SAPLING, line));
                case String s when s.contains("fuel") -> this.addComponent(new IcoTextComponent(Ico.FURNACE, line));
                case String s when s.contains("time") -> this.addComponent(new IcoTextComponent(Ico.CLOCK, line));
                case String s when s.contains("stored") -> this.addComponent(new IcoTextComponent(Ico.COMPOSTER, line));
                default -> this.addComponent(new PlainTextComponent(line));
            }
        }

        this.addSimpleIcoText(Ico.SAPLING, "Organic Matter:", Formatting.YELLOW, 48 + offset);
        this.addSimpleIcoText(Ico.FURNACE, "Fuel:", Formatting.BLUE, 49 + offset);
        this.addSimpleIcoText(Ico.CLOCK, "Time Left:", Formatting.RED, 50 + offset);
        this.addSimpleIcoText(Ico.COMPOSTER, "Stored Compost:", Formatting.DARK_GREEN, 51 + offset);
    }
}
