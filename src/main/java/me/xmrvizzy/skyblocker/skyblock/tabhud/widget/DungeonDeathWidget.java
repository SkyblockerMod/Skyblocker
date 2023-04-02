package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows various dungeon info
// deaths, healing, dmg taken, milestones

public class DungeonDeathWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Death").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // match the deaths entry
    // group 1: amount of deaths
    private static final Pattern DEATH_PATTERN = Pattern.compile("\\S*: \\((\\d+)\\).*");

    public DungeonDeathWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        Matcher m = StrMan.regexAt(list, 25, DEATH_PATTERN);
        Formatting f = (m.group(1).equals("0")) ? Formatting.GREEN : Formatting.RED;
        Text d = Text.literal("Deaths: ").append(Text.literal(m.group(1)).formatted(f));
        IcoTextComponent deaths = new IcoTextComponent(Ico.SKULL, d);
        this.addComponent(deaths);

        Text dealt = StrMan.stdEntry(list, 26, "Damage Dealt:", Formatting.RED);
        IcoTextComponent de = new IcoTextComponent(Ico.SWORD, dealt);
        this.addComponent(de);

        Text heal = StrMan.stdEntry(list, 27, "Healing Done:", Formatting.RED);
        IcoTextComponent he = new IcoTextComponent(Ico.POTION, heal);
        this.addComponent(he);

        Text mile = StrMan.stdEntry(list, 28, "Milestone:", Formatting.YELLOW);
        IcoTextComponent mi = new IcoTextComponent(Ico.NTAG, mile);
        this.addComponent(mi);

        this.pack();

    }

}
