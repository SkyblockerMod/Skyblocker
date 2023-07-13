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
    // group 1: player name
    // group 2: last seen, if owner not online
    // ^(?<nameA>.*) \((?<lastseen>.*)\)$|^\[\d*\] (?:\[[A-Za-z]+\] )?(?<nameB>[A-Za-z0-9_]*)(?: .*)?$|^(?<nameC>.*)$
    private static final Pattern OWNER_PATTERN = Pattern
            .compile("^(?<nameA>.*) \\((?<lastseen>.*)\\)$|^\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?(?<nameB>[A-Za-z0-9_]*)(?: .*)?$|^(?<nameC>.*)$");

    public IslandOwnersWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
        for (int i = 1; i < 20; i++) {
            Matcher m = PlayerListMgr.regexAt(i, OWNER_PATTERN);
            if (m == null) {
                break;
            }

            String name = null, lastseen = null;
            Formatting format = null;
            if (m.group("nameA") != null) {
                name = m.group("nameA");
                lastseen = m.group("lastseen");
                format = Formatting.GRAY;
            } else if (m.group("nameB")!=null){
                name = m.group("nameB");
                lastseen = "Online";
                format = Formatting.WHITE;
            } else {
                name = m.group("nameC");
                lastseen = "Online";
                format = Formatting.WHITE;
            }

            Text entry = Text.literal(name)
                    .append(
                            Text.literal(" (" + lastseen + ")")
                                    .formatted(format));
            PlainTextComponent ptc = new PlainTextComponent(entry);
            this.addComponent(ptc);
        }
        this.pack();
    }

}
