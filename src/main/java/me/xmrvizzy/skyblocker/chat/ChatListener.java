package me.xmrvizzy.skyblocker.chat;

import java.util.regex.Pattern;

public abstract class ChatListener {
    protected static final String NUMBER = "-?[0-9]{1,3}(?>,[0-9]{3})*(?:\\.[1-9])?";
    private Pattern pattern;
    public ChatListener(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }
    public Pattern getPattern() {
        return pattern;
    }

    public abstract boolean isEnabled();
    //Returns whether message should get filtered
    public boolean onMessage(String[] groups) {
        return true;
    }
}
