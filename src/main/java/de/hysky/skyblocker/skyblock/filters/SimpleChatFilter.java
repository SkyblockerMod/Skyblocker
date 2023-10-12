package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

public abstract class SimpleChatFilter extends ChatPatternListener {
    public SimpleChatFilter(String pattern) {
        super(pattern);
    }

    @Override
    protected final boolean onMatch(Text message, Matcher matcher) {
        return true;
    }
}
