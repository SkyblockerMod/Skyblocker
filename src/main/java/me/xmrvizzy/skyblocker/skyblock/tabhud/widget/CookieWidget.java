package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about active super cookies
// or not, if you're unwilling to buy one
// TODO: test with active cookie. I'm not buying one.

public class CookieWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Cookie Info").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    public CookieWidget(String footertext) {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());

        String interesting = footertext.split("Cookie Buff")[1];
        String[] lines = interesting.split("\n");
        if (lines[1].startsWith("Not")) {
            this.addComponent(new IcoTextComponent(Ico.COOKIE, Text.of("Not active")));
        } else {
            Text cookie = Text.literal("Time Left: ").append(lines[1]);
            this.addComponent(new IcoTextComponent(Ico.COOKIE, cookie));
        }
        this.pack();
    }

}
