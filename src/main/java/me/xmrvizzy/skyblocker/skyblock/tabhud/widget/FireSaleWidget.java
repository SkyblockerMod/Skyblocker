package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Formatting;

// this widget shows info about fire sales when in the hub.
// or not, if there isn't one going on

public class FireSaleWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Fire Sale").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    // matches a fire sale item
    // group 1: item name
    // group 2: # items available
    // group 3: # items available in total (1 digit + "k")
    private static final Pattern FIRE_PATTERN = Pattern.compile("(?<item>.*): (?<avail>\\d*)/(?<total>[0-9.]*)k");

    public FireSaleWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        String event = PlayerListMgr.strAt(46);

        if (event == null) {
            this.addComponent(new PlainTextComponent(Text.literal("No Fire Sale!").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }

        if (event.contains("Starts In")) {
            this.addSimpleIcoText(Ico.CLOCK, "Starts in:", Formatting.DARK_AQUA, 46);
            this.pack();
            return;
        }

        for (int i = 46;; i++) {
            Matcher m = PlayerListMgr.regexAt( i, FIRE_PATTERN);
            if (m == null) {
                break;
            }
            String avail = m.group("avail");
            Text itemTxt = Text.literal(m.group("item"));
            float total = Float.parseFloat(m.group("total")) * 1000;
            Text prgressTxt = Text.literal(String.format("%s/%.0f", avail, total));
            float pcnt = (Float.parseFloat(avail) / (total)) * 100f;
            ProgressComponent pc = new ProgressComponent(Ico.GOLD, itemTxt, prgressTxt, pcnt, pcntToCol(pcnt));
            this.addComponent(pc);
        }
        this.pack();

    }

    private int pcntToCol(float pcnt) {
        return MathHelper.hsvToRgb( pcnt / 300f, 0.9f, 0.9f);
    }

}
