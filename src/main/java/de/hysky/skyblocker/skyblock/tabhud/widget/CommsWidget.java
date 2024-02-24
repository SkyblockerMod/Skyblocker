package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Colors;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows the status of the king's commissions.
// (dwarven mines and crystal hollows)

public class CommsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Commissions").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // match a comm
    // group 1: comm name
    // group 2: comm progress (without "%" for comms that show a percentage)
    private static final Pattern COMM_PATTERN = Pattern.compile("(?<name>.*): (?<progress>.*)%?");

    public CommsWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    private static ProgressComponent getFancyComponent(String name, String progress) {
        if (progress.equals("DONE")) {
            return new ProgressComponent(Ico.BOOK, Text.of(name), Text.of(progress), 100f, Colors.pcntToCol(100));
        } else {
            float pcnt = Float.parseFloat(progress.substring(0, progress.length() - 1));
            return new ProgressComponent(Ico.BOOK, Text.of(name), pcnt, Colors.pcntToCol(pcnt));
        }
    }

    private static PlainTextComponent getComponent(String name, String progress) {
        float pcnt = Float.parseFloat(progress.substring(0, progress.length() - 1));
        return new PlainTextComponent(Text.literal(name + ": ").append(Text.literal(progress)
                .formatted(Colors.hypixelProgressColor(pcnt))));
    }

    @Override
    public void updateContent() {
        SkyblockerConfig.DwarvenHudStyle style = SkyblockerConfigManager.get().locations.dwarvenMines.dwarvenHud.style;
        boolean isFancy = style == SkyblockerConfig.DwarvenHudStyle.FANCY;

        for (int i = 50; i <= 53; i++) {
            Matcher m = PlayerListMgr.regexAt(i, COMM_PATTERN);
            // end of comms found?
            if (m == null) {
                if (i == 50) {
                    this.addComponent(new IcoTextComponent());
                }
                break;
            }

            String name = m.group("name");
            String progress = m.group("progress");

            Component component;
            if (isFancy) component = getFancyComponent(name, progress);
            else component = getComponent(name, progress);

            this.addComponent(component);
        }
    }

}
