package de.hysky.skyblocker.skyblock.tabhud.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about active super cookies
// or not, if you're unwilling to buy one

public class CookieWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Cookie Info").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    private static final Pattern COOKIE_PATTERN = Pattern.compile(".*\\nCookie Buff\\n(?<buff>.*)\\n");

    public CookieWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        String footertext = PlayerListMgr.getFooter();
        if (footertext == null || !footertext.contains("Cookie Buff")) {
            this.addComponent(new IcoTextComponent());
            return;
        }

        Matcher m = COOKIE_PATTERN.matcher(footertext);
        if (!m.find() || m.group("buff") == null) {
            this.addComponent(new IcoTextComponent());
            return;
        }

        String buff = m.group("buff");
        if (buff.startsWith("Not")) {
            this.addComponent(new IcoTextComponent(Ico.COOKIE, Text.of("Not active")));
        } else {
            Text cookie = Text.literal("Time Left: ").append(buff);
            this.addComponent(new IcoTextComponent(Ico.COOKIE, cookie));
        }
    }

}
