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

// this widget shows a list of the owners while on your home island

public class IslandSelfWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Owners").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // matches an owner
    // group 1: player name
    private static final Pattern OWNER_PATTERN = Pattern.compile("\\[\\d*\\] (.*)");

    public IslandSelfWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        for (int i = 1; i < 20; i++) {
            String str = list.get(i).getDisplayName().getString();
            if (str.length() == 0) {
                break;
            }
            Matcher m = StrMan.regexAt(list, i, OWNER_PATTERN);
            PlainTextComponent ptc = new PlainTextComponent(Text.of(m.group(1)));
            this.addComponent(ptc);
        }
        this.pack();
    }

}
