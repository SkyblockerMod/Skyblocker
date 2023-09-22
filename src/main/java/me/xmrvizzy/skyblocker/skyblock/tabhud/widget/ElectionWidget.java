package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows the status or results of the current election

public class ElectionWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Election Info").formatted(Formatting.YELLOW,
            Formatting.BOLD);

    private static final HashMap<String, ItemStack> MAYOR_DATA = new HashMap<>();

    private static final Text EL_OVER = Text.literal("Election ")
            .append(Text.literal("over!").formatted(Formatting.RED));

    // pattern matching a candidate while people are voting
    // group 1: name
    // group 2: % of votes
    private static final Pattern VOTE_PATTERN = Pattern.compile("(?<mayor>\\S*): \\|+ \\((?<pcnt>\\d*)%\\)");

    static {
        MAYOR_DATA.put("Aatrox", Ico.DIASWORD);
        MAYOR_DATA.put("Cole", Ico.PICKAXE);
        MAYOR_DATA.put("Diana", Ico.BONE);
        MAYOR_DATA.put("Diaz", Ico.GOLD);
        MAYOR_DATA.put("Finnegan", Ico.HOE);
        MAYOR_DATA.put("Foxy", Ico.SUGAR);
        MAYOR_DATA.put("Paul", Ico.COMPASS);
        MAYOR_DATA.put("Scorpius", Ico.MOREGOLD);
        MAYOR_DATA.put("Jerry", Ico.VILLAGER);
        MAYOR_DATA.put("Derpy", Ico.DBUSH);
        MAYOR_DATA.put("Marina", Ico.FISH_ROD);
    }

    private static final Formatting[] COLS = {Formatting.GOLD, Formatting.RED, Formatting.LIGHT_PURPLE};

    public ElectionWidget() {
        super(TITLE, Formatting.YELLOW.getColorValue());
    }

    @Override
    public void updateContent() {
        String status = PlayerListMgr.strAt(76);
        if (status == null) {
            this.addComponent(new IcoTextComponent());
            this.addComponent(new IcoTextComponent());
            this.addComponent(new IcoTextComponent());
            this.addComponent(new IcoTextComponent());
            return;
        }

        if (status.contains("Over!")) {
            // election is over
            IcoTextComponent over = new IcoTextComponent(Ico.BARRIER, EL_OVER);
            this.addComponent(over);

            String win = PlayerListMgr.strAt(77);
            if (win == null || !win.contains(": ")) {
                this.addComponent(new IcoTextComponent());
            } else {
                String winnername = win.split(": ")[1];
                Text winnertext = Widget.simpleEntryText(winnername, "Winner: ", Formatting.GREEN);
                IcoTextComponent winner = new IcoTextComponent(MAYOR_DATA.get(winnername), winnertext);
                this.addComponent(winner);
            }

            this.addSimpleIcoText(Ico.PLAYER, "Participants:", Formatting.AQUA, 78);
            this.addSimpleIcoText(Ico.SIGN, "Year:", Formatting.LIGHT_PURPLE, 79);

        } else {
            // election is going on
            this.addSimpleIcoText(Ico.CLOCK, "End in:", Formatting.GOLD, 76);

            for (int i = 77; i <= 79; i++) {
                Matcher m = PlayerListMgr.regexAt(i, VOTE_PATTERN);
                if (m == null) {
                    this.addComponent(new ProgressComponent());
                } else {

                    String mayorname = m.group("mayor");
                    String pcntstr = m.group("pcnt");
                    float pcnt = Float.parseFloat(pcntstr);
                    Text candidate = Text.literal(mayorname).formatted(COLS[i - 77]);
                    ProgressComponent pc = new ProgressComponent(MAYOR_DATA.get(mayorname), candidate, pcnt,
                            COLS[i - 77].getColorValue());
                    this.addComponent(pc);
                }
            }
        }
    }

}
