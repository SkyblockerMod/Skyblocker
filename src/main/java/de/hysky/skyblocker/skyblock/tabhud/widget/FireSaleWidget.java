package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about fire sales when in the hub.
// or not, if there isn't one going on

public class FireSaleWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Fire Sales").formatted(Formatting.DARK_AQUA,
            Formatting.BOLD);

    public FireSaleWidget() {
        super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
        Text event = PlayerListMgr.textAt(46);

        if (event == null) {
            this.addComponent(new PlainTextComponent(Text.literal("No Fire Sales!").formatted(Formatting.GRAY)));
            return;
        }

        if (event.getString().contains("starting in")) {
            this.addComponent(new IcoTextComponent(Ico.CLOCK, event));
            return;
        }
    }
}
