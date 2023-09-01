package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// empty widget for when nothing can be shown

public class ErrorWidget extends Widget {
    private static final MutableText TITLE = Text.literal("Error").formatted(Formatting.RED,
            Formatting.BOLD);

    Text error = Text.of("No info available!");

    public ErrorWidget() {
        super(TITLE, Formatting.RED.getColorValue());
    }

    public ErrorWidget(String error) {
        super(TITLE, Formatting.RED.getColorValue());
        this.error = Text.of(error);
    }

    @Override
    public void updateContent() {
        PlainTextComponent inf = new PlainTextComponent(this.error);
        this.addComponent(inf);
    }

}
