package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// empty widget for when nothing can be shown

public class EmptyWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Empty").formatted(Formatting.RED,
            Formatting.BOLD);

    public EmptyWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.RED.getColorValue());

        Text info = Text.of("No info for this area!");
        PlainTextComponent inf = new PlainTextComponent(info);
        this.addComponent(inf);
        this.pack();
    }

}
