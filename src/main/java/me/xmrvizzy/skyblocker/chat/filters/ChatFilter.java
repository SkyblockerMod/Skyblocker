package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;

public abstract class ChatFilter extends ChatListener {
    public ChatFilter(String pattern) {
        super(pattern);
    }
    public boolean onMessage(String[] groups) {
        return true;
    }
}
