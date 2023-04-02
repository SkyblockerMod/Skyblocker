package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about current profile/account upgrades
// or not, if there aren't any
// TODO: not very pretty atm

public class UpgradeWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Upgrade Info").formatted(Formatting.GOLD,
            Formatting.BOLD);

    public UpgradeWidget(String footertext) {
        super(TITLE, Formatting.GOLD.getColorValue());
        String[] interesting = footertext.split("Upgrades");
        if (interesting.length < 2) {
            this.addComponent(new PlainTextComponent(Text.of("Currently no upgrades...")));
            this.pack();
            return;
        }
        String[] lines = interesting[1].split("\n");
        IcoTextComponent u1 = new IcoTextComponent(Ico.SIGN, Text.of(lines[1]));
        this.addComponent(u1);
        if (lines.length == 5) {
            IcoTextComponent u2 = new IcoTextComponent(Ico.SIGN, Text.of(lines[2]));
            this.addComponent(u2);
        }
        this.pack();
    }

}
