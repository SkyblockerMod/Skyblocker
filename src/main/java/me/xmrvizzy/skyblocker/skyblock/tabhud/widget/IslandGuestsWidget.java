package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows a list of all people visiting 
// the same private island as you

public class IslandGuestsWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Guests").formatted(Formatting.AQUA,
            Formatting.BOLD);

    // matches a player entry, removing their level and the hand icon
    // group 1: player name
    private static final Pattern GUEST_PATTERN = Pattern.compile("\\[\\d*\\] (.*) \\[.\\]");

    public IslandGuestsWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.AQUA.getColorValue());
        for (int i = 21; i < 40; i++) {
            String str = list.get(i).getDisplayName().getString();
            if (str.length() == 0) {
                if (i == 21) {
                    this.addComponent(new PlainTextComponent(Text.literal("No Visitors!").formatted(Formatting.GRAY)));
                }
                break;
            }
            Matcher m = StrMan.regexAt(list, i, GUEST_PATTERN);
            PlainTextComponent ptc = new PlainTextComponent(Text.of(m.group(1)));
            this.addComponent(ptc);
        }
        this.pack();
    }

}
