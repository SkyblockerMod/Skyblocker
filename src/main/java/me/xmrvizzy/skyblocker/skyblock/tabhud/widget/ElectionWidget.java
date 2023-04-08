package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;

import net.minecraft.client.network.PlayerListEntry;
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
    private static final Pattern VOTE_PATTERN = Pattern.compile(" (\\S*): \\|+ \\((\\d*)%\\)");

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

    private static final Formatting[] COLS = { Formatting.GOLD, Formatting.RED, Formatting.LIGHT_PURPLE };

    public ElectionWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        if (StrMan.strAt(list, 76).contains("Over!")) {
            // election is over
            IcoTextComponent over = new IcoTextComponent(Ico.BARRIER, EL_OVER);
            this.addComponent(over);

            String winnername = StrMan.strAt(list, 77).split(": ")[1];
            Text winnertext = Text.literal("Winner: ")
                    .append(Text.literal(winnername).formatted(Formatting.GREEN));
            IcoTextComponent winner = new IcoTextComponent(MAYOR_DATA.get(winnername), winnertext);
            this.addComponent(winner);

            Text participants = StrMan.stdEntry(list, 78, "Participants:", Formatting.AQUA);
            IcoTextComponent part = new IcoTextComponent(Ico.PLAYER, participants);
            this.addComponent(part);

            Text year = StrMan.stdEntry(list, 79, "Year:", Formatting.LIGHT_PURPLE);
            IcoTextComponent y = new IcoTextComponent(Ico.SIGN, year);
            this.addComponent(y);

        } else {
            // election is going on
            Text time = StrMan.stdEntry(list, 76, "End in:", Formatting.GOLD);
            IcoTextComponent t = new IcoTextComponent(Ico.CLOCK, time);
            this.addComponent(t);

            for (int i = 77; i <= 79; i++) {
                Matcher m = StrMan.regexAt(list, i, VOTE_PATTERN);
                String g1 = m.group(1);
                String g2 = m.group(2);
                float pcnt = Float.parseFloat(g2);
                Text candidate = Text.literal(g1).formatted(COLS[i - 77]);
                ProgressComponent pc = new ProgressComponent(MAYOR_DATA.get(g1), candidate, pcnt,
                        COLS[i - 77].getColorValue());
                this.addComponent(pc);

            }
        }
        this.pack();
    }

}
