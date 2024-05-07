package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

public class CommsWidget extends TabHudWidget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // match a comm
    // group 1: comm name
    // group 2: comm progress (without "%" for comms that show a percentage)
    private static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

    public CommsWidget() {
        super("Commissions", TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent(List<Text> lines) {
        if (lines.isEmpty()) {
            this.addComponent(new IcoTextComponent());
            return;
        }
        for (Text line : lines) {
            Matcher m = COMM_PATTERN.matcher(line.getString());
            if (m.matches()) {
                ProgressComponent pc;

                String name = m.group("name");
                String progress = m.group("progress");

                if (progress.equals("DONE")) {
                    pc = new ProgressComponent(Ico.BOOK, Text.of(name), Text.of(progress), 100f, Colors.pcntToCol(100));
                } else {
                    float pcnt = Float.parseFloat(progress.substring(0, progress.length() - 1));
                    pc = new ProgressComponent(Ico.BOOK, Text.of(name), pcnt, Colors.pcntToCol(pcnt));
                }
                this.addComponent(pc);
            }
        }
    }

}
