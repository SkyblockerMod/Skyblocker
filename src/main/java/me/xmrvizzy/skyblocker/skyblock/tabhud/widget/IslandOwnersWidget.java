package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows a list of the owners of a home island while guesting

public class IslandOwnersWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Owners").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // matches an owner
    // group 1: player name (cut off by hypixel for some reason)
    // group 2: last seem
    // TODO: test with owner online
    private static final Pattern OWNER_PATTERN = Pattern.compile("(?<name>.*) \\((?<lastseen>.*)\\)");

    public IslandOwnersWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        for (int i = 1; i < 20; i++) {
            Matcher m = PlayerListMgr.regexAt( i, OWNER_PATTERN);
            if (m == null) {
                break;
            }

            Text entry = Text.literal(m.group("name"))
                    .append(
                            Text.literal(" (" + m.group("lastseen") + ")")
                                    .formatted(Formatting.GRAY));
            PlainTextComponent ptc = new PlainTextComponent(entry);
            this.addComponent(ptc);
        }
        this.pack();
    }

}
