package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.dwarven.CommissionLabels;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

@RegisterWidget
public class CommsWidget extends TabHudWidget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // match a comm
    // group 1: comm name
    // group 2: comm progress (without "%" for comms that show a percentage)
    private static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

    private final List<Commission> commissions = new ArrayList<>(4);
    private boolean oldDone = false;

    public CommsWidget() {
        super("Commissions", TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent(List<Text> lines) {
        if (lines.isEmpty()) {
            this.addComponent(new IcoTextComponent());
            return;
        }
        List<String> oldCommissionNames = commissions.stream().map(Commission::name).toList();
        List<String> newCommissionsNames = new ArrayList<>(commissions.size());
        commissions.clear();
        boolean commissionDone = false;
        for (Text line : lines) {
            Matcher m = COMM_PATTERN.matcher(line.getString());
            if (m.matches()) {
                ProgressComponent pc;

                String name = m.group("name");
                String progress = m.group("progress");
                commissions.add(new Commission(name, progress));
                newCommissionsNames.add(name);

                if (progress.equals("DONE")) {
                    pc = new ProgressComponent(Ico.BOOK, Text.of(name), Text.of(progress), 100f, ColorUtils.percentToColor(100));
                    commissionDone = true;
                } else {
                    float pcnt = Float.parseFloat(progress.substring(0, progress.length() - 1));
                    pc = new ProgressComponent(Ico.BOOK, Text.of(name), pcnt, ColorUtils.percentToColor(pcnt));
                }
                this.addComponent(pc);
            }
        }
        if (!oldCommissionNames.equals(newCommissionsNames) || oldDone != commissionDone) {
            CommissionLabels.update(newCommissionsNames, commissionDone);
        }
        oldDone = commissionDone;
    }

    record Commission(String name, String progress){}

}
