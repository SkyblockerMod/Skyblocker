package de.hysky.skyblocker.skyblock.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChatRulesHandlerTest {

    @Test
    void formatText() {
        //generate test text
        MutableText testText = Text.empty();
        Style style = Style.EMPTY.withFormatting(Formatting.DARK_BLUE);
        Text.of("test").getWithStyle(style).forEach(testText::append);
        style = style.withFormatting(Formatting.UNDERLINE);
        Text.of("line").getWithStyle(style).forEach(testText::append);
        style = style.withFormatting(Formatting.DARK_GREEN);
        Text.of("dark green").getWithStyle(style).forEach(testText::append);
        style = style.withFormatting(Formatting.ITALIC);
        Text.of("italic").getWithStyle(style).forEach(testText::append);

        //generated text
        MutableText text = ChatRulesHandler.formatText("&1test&nline&2dark green&oitalic");

        Assertions.assertEquals(text, testText);

    }
}
