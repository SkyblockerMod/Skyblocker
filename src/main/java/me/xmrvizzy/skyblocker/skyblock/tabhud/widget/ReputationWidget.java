package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.ProgressComponent;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows your faction status

public class ReputationWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Faction Status").formatted(Formatting.AQUA,
            Formatting.BOLD);

    private static final Pattern PROGRESS_PATTERN = Pattern.compile(" \\|+ \\(([0-9.]*)%\\)");
    private static final Pattern STATE_PATTERN = Pattern.compile("(\\S*) *(\\S*)");

    public ReputationWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.AQUA.getColorValue());

        String fname = StrMan.strAt(list, 45).split(" ")[0];
        String rep = StrMan.strAt(list, 46).trim();
        Matcher prog = StrMan.regexAt(list, 47, PROGRESS_PATTERN);
        Matcher state = StrMan.regexAt(list, 48, STATE_PATTERN);

        IcoTextComponent faction;
        if (fname.equals("Mage")) {
            faction = new IcoTextComponent(Ico.POTION, Text.literal(fname).formatted(Formatting.DARK_AQUA));
        } else {
            faction = new IcoTextComponent(Ico.SWORD, Text.literal(fname).formatted(Formatting.RED));
        }
        this.addComponent(faction);

        float pcnt = Float.parseFloat(prog.group(1));

        ProgressComponent pc = new ProgressComponent(Ico.LANTERN, Text.of(state.group(1) + " -> " + state.group(2)), Text.of(rep), pcnt, Formatting.AQUA.getColorValue());
        this.addComponent(pc);

        this.pack();

    }

}
