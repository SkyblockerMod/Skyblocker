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

// this widget shows info about the garden server

public class GardenServerWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Server Info").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    private static final Pattern VISITOR_PATTERN = Pattern.compile(" Next Visitor: (.*)$");

    public GardenServerWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());

        Text areaName = StrMan.stdEntry(list, 41, "Area:", Formatting.DARK_AQUA);
        IcoTextComponent area = new IcoTextComponent(Ico.MAP, areaName);
        this.addComponent(area);

        Text serverName = StrMan.stdEntry(list, 42, "Server ID:", Formatting.GRAY);
        IcoTextComponent server = new IcoTextComponent(Ico.NTAG, serverName);
        this.addComponent(server);

        Text amtGems = StrMan.stdEntry(list, 43, "Gems:", Formatting.GREEN);
        IcoTextComponent gems = new IcoTextComponent(Ico.EMERALD, amtGems);
        this.addComponent(gems);

        Text copper = StrMan.stdEntry(list, 44, "Copper:", Formatting.GOLD);
        IcoTextComponent co = new IcoTextComponent(Ico.COPPER, copper);
        this.addComponent(co);

        Matcher v = StrMan.regexAt(list, 45, VISITOR_PATTERN);
        String vString = v.group(1);
        Formatting col;
        if (vString.equals("Not Unlocked!")) {
            col = Formatting.RED;
        } else {
            col = Formatting.GREEN;
        }
        MutableText visitor = Text.literal("Next Visitor: ")
                .append(Text.literal(vString).formatted(col));
        IcoTextComponent vis = new IcoTextComponent(Ico.PLAYER, visitor);
        this.addComponent(vis);
        this.pack();
    }

}
