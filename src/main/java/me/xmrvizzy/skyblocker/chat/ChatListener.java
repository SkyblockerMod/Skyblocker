package me.xmrvizzy.skyblocker.chat;

import java.util.regex.Pattern;

public abstract class ChatListener {
    Pattern pattern;
    public ChatListener(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }
    public Pattern getPattern() {
        return pattern;
    }

    public abstract boolean isEnabled();
    //Returns whether message should get filtered
    public abstract boolean onMessage(String[] groups);
}
