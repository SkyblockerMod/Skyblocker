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

// this widget shows a list of the owners of a home island while guesting

public class IslandOwnersWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Owners").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // matches an owner
    // group 1: player name (cut off by hypixel for some reason)
    // group 2: last seem
    // TODO: test with owner online
    private static final Pattern OWNER_PATTERN = Pattern.compile("(.*) \\((.*)\\)");

    public IslandOwnersWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        for (int i = 1; i < 20; i++) {
            if (list.get(i).getDisplayName().getString().length() == 0) {
                break;
            }

            Matcher m = StrMan.regexAt(list, i, OWNER_PATTERN);
            Text entry = Text.literal(m.group(1))
                    .append(
                            Text.literal(" (" + m.group(2) + ")")
                                    .formatted(Formatting.GRAY));
            PlainTextComponent ptc = new PlainTextComponent(entry);
            this.addComponent(ptc);
        }
        this.pack();
    }

}
