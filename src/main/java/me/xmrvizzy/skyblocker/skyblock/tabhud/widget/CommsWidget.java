package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

// this widget shows the status of the king's commissions.
// works in both the dwarven mines and the CH

public class CommsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // match a comm
    // group 1: comm name
    // group 2: comm progress (without "%" for comms that show a percentage)
    private static final Pattern COMM_PATTERN = Pattern.compile(" (.*): (.*)%?");

    public CommsWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        for (int i = 50; i <= 51; i++) {
            Matcher m = StrMan.regexAt(list, i, COMM_PATTERN);

            String g2 = m.group(2);
            ProgressComponent pc;
            if (g2.equals("DONE")) {
                pc = new ProgressComponent(Ico.BOOK, Text.of(m.group(1)), Text.of(g2), 100f, pcntToCol(100));
            } else {
                float pcnt = Float.parseFloat(g2.substring(0, g2.length() - 1));
                pc = new ProgressComponent(Ico.BOOK, Text.of(m.group(1)), pcnt, pcntToCol(pcnt));
            }
            this.addComponent(pc);
        }
        this.pack();
    }

    private int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb(pcnt / 300f, 0.9f, 0.9f);
    }

}
