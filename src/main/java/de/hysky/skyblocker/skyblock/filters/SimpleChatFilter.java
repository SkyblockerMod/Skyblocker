package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.text.Text;
import org.intellij.lang.annotations.Language;

import java.util.regex.Matcher;

public abstract class SimpleChatFilter extends ChatPatternListener {
    protected SimpleChatFilter(@Language("RegExp") String pattern) {
        super(pattern);
    }

    @Override
    protected final boolean onMatch(Text message, Matcher matcher) {
        return true;
    }
}
