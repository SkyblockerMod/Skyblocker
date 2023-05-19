package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about ongoing profile/account upgrades
// or not, if there aren't any
// TODO: not very pretty atm

public class UpgradeWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Upgrade Info").formatted(Formatting.GOLD,
            Formatting.BOLD);

    public UpgradeWidget(String footertext) {
        super(TITLE, Formatting.GOLD.getColorValue());

        if (footertext == null || !footertext.contains("Upgrades")) {
            this.addComponent(new PlainTextComponent(Text.literal("No data").formatted(Formatting.GRAY)));
            this.pack();
            return;
        }

        String[] interesting = footertext.split("Upgrades");
        this.addComponent(new PlainTextComponent(Text.of("Currently no upgrades...")));
        this.pack();

        String[] lines = interesting[1].split("\n");
        IcoTextComponent u1 = new IcoTextComponent(Ico.SIGN, Text.of(lines[1]));
        this.addComponent(u1);
        if (lines.length == 5) { // ??? no idea how this works, but it does. don't touch until understood...
            IcoTextComponent u2 = new IcoTextComponent(Ico.SIGN, Text.of(lines[2]));
            this.addComponent(u2);
        }
        this.pack();
    }

}
