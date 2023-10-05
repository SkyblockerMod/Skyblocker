package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Comparator;

// this widget shows a list of obtained dungeon buffs

public class DungeonBuffWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Dungeon Buffs").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public DungeonBuffWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {

        String footertext = PlayerListMgr.getFooter();

        if (footertext == null || !footertext.contains("Dungeon Buffs")) {
            this.addComponent(new PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)));
            return;
        }

        String interesting = footertext.split("Dungeon Buffs")[1];
        String[] lines = interesting.split("\n");

        if (!lines[1].startsWith("Blessing")) {
            this.addComponent(new PlainTextComponent(Text.literal("No buffs found!").formatted(Formatting.GRAY)));
            return;
        }

        //Filter out text unrelated to blessings
        lines = Arrays.stream(lines).filter(s -> s.contains("Blessing")).toArray(String[]::new);

        //Alphabetically sort the blessings
        Arrays.sort(lines, Comparator.comparing(String::toLowerCase));

        for (String line : lines) {
            if (line.length() < 3) { // empty line is §s
                break;
            }
            int color = getBlessingColor(line);
            this.addComponent(new PlainTextComponent(Text.literal(line).styled(style -> style.withColor(color))));
        }

    }

    @SuppressWarnings("DataFlowIssue")
    public int getBlessingColor(String blessing) {
        if (blessing.contains("Life")) return Formatting.LIGHT_PURPLE.getColorValue();
        if (blessing.contains("Power")) return Formatting.RED.getColorValue();
        if (blessing.contains("Stone")) return Formatting.GREEN.getColorValue();
        if (blessing.contains("Time")) return 0xafb8c1;
        if (blessing.contains("Wisdom")) return Formatting.AQUA.getColorValue();

        return 0xffffff;
    }

}