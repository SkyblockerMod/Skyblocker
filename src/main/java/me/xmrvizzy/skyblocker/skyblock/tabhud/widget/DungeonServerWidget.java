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

// this widget shows broad info about the current dungeon
// opened/completed rooms, % of secrets found and time taken

public class DungeonServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Dungeon Info").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // match the secrets text
    // group 1: % of secrets found (without "%")
    private static final Pattern SECRET_PATTERN = Pattern.compile(" Secrets Found: (.*)%");

    public DungeonServerWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        Text name = StrMan.stdEntry(list, 41, "Name:", Formatting.AQUA);
        IcoTextComponent na = new IcoTextComponent(Ico.NTAG, name);
        this.addComponent(na);

        Text open = StrMan.stdEntry(list, 42, "Rooms Visited:", Formatting.DARK_PURPLE);
        IcoTextComponent op = new IcoTextComponent(Ico.SIGN, open);
        this.addComponent(op);

        Text compl = StrMan.stdEntry(list, 43, "Rooms Completed:", Formatting.LIGHT_PURPLE);
        IcoTextComponent co = new IcoTextComponent(Ico.SIGN, compl);
        this.addComponent(co);

        Matcher m = StrMan.regexAt(list, 44, SECRET_PATTERN);
        Text secrets = Text.of("Secrets found:");
        ProgressComponent scp = new ProgressComponent(Ico.CHEST, secrets, Float.parseFloat(m.group(1)),
                Formatting.DARK_PURPLE.getColorValue());
        this.addComponent(scp);

        Text time = StrMan.stdEntry(list, 45, "Time:", Formatting.GOLD);
        IcoTextComponent ti = new IcoTextComponent(Ico.CLOCK, time);
        this.addComponent(ti);

        this.pack();
    }

}
