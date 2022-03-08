package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
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
