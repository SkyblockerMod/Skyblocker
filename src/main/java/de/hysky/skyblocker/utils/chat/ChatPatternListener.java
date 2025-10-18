package de.hysky.skyblocker.utils.chat;

import net.minecraft.text.Text;
import org.intellij.lang.annotations.Language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ChatPatternListener implements ChatMessageListener {
    protected static final String NUMBER = "-?[0-9]{1,3}(?>,[0-9]{3})*(?:\\.[1-9])?";
    public final Pattern pattern;

    protected ChatPatternListener(@Language("RegExp") String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public final ChatFilterResult onMessage(Text message, String asString) {
        ChatFilterResult state = state();
        if (state == ChatFilterResult.PASS) return ChatFilterResult.PASS;
        Matcher m = pattern.matcher(asString);
        if (m.matches() && onMatch(message, m)) {
            return state;
        }
        return ChatFilterResult.PASS;
    }

    protected abstract ChatFilterResult state();

    protected abstract boolean onMatch(Text message, Matcher matcher);
}
