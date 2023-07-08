package me.xmrvizzy.skyblocker.utils.title;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Title {
    private MutableText text;
    protected float lastX = 0;
    protected float lastY = 0;

    public MutableText getText() {
        return text;
    }

    public void setText(MutableText text) {
        this.text = text;
    }

    public void setFormatting(Formatting formatting) {
        this.text.formatted(formatting);
    }

    public Title(String textKey, Formatting formatting) {
        this(Text.translatable(textKey).formatted(formatting));
    }

    public Title(MutableText text) {
        this.text = text;
    }
}
