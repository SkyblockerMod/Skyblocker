package me.xmrvizzy.skyblocker.utils.title;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Represents a title used for {@link TitleContainer}.
 *
 * @see TitleContainer
 */
public class Title {
    private MutableText text;
    protected float x = -1;
    protected float y = -1;

    /**
     * Constructs a new title with the given translation key and formatting to be applied.
     *
     * @param textKey    the translation key
     * @param formatting the formatting to be applied to the text
     */
    public Title(String textKey, Formatting formatting) {
        this(Text.translatable(textKey).formatted(formatting));
    }

    /**
     * Constructs a new title with the given {@link MutableText}.
     * Use {@link Text#literal(String)} or {@link Text#translatable(String)} to create a {@link MutableText}
     *
     * @param text the mutable text
     */
    public Title(MutableText text) {
        this.text = text;
    }

    public MutableText getText() {
        return text;
    }

    public void setText(MutableText text) {
        this.text = text;
    }

    protected boolean isDefaultPos() {
        return x == -1 && y == -1;
    }

    protected void resetPos() {
        this.x = -1;
        this.y = -1;
    }
}
