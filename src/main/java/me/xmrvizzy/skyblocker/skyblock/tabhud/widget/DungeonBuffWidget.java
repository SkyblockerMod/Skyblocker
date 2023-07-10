package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows a list of obtained dungeon buffs
// TODO: could be more pretty, can't be arsed atm

public class DungeonBuffWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Dungeon Buffs").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonBuffWidget(String footertext) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        if (footertext == null || !footertext.contains("Dungeon Buffs")) {
            this.addComponent(new PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }

        String interesting = footertext.split("Dungeon Buffs")[1];
        String[] lines = interesting.split("\n");

        if (!lines[1].startsWith("Blessing")) {
            this.addComponent(new PlainTextComponent(Text.literal("No buffs found!").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].length() < 3) { // empty line is §s
                break;
            }
            this.addComponent(new PlainTextComponent(Text.of(lines[i])));
        }

        this.pack();
    }

}
