package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// empty widget for when nothing can be shown

public class EmptyWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Empty").formatted(Formatting.RED,
            Formatting.BOLD);

    public EmptyWidget() {
        super(TITLE, Formatting.RED.getColorValue());
    }

    @Override
    public void update() {
        super.update();
        Text info = Text.of("No info for this area!");
        PlainTextComponent inf = new PlainTextComponent(info);
        this.addComponent(inf);
        this.pack();
    }

}
