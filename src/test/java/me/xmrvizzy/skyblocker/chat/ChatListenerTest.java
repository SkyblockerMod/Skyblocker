package me.xmrvizzy.skyblocker.chat;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ChatListenerTest<T extends ChatListener> {
    protected final T listener;

    public ChatListenerTest(T listener) {
        this.listener = listener;
    }

    protected boolean captures(String text) {
        return listener.getPattern().matcher(text).matches();
    }
    protected String[] getGroups(String text) {
        Matcher matcher = listener.getPattern().matcher(text);
        assertTrue(matcher.matches());
        String[] groups = new String[matcher.groupCount() + 1];
        for (int i = 0; i < groups.length; i++)
            groups[i] = matcher.group(i);
        return groups;
    }
    protected void assertCaptures(String text) {
        assertTrue(captures(text));
    }
    protected void assertNotCaptures(String text) {
        assertTrue(captures(text));
    }
    protected void assertGroup(String text, int group, String expect) {
        assertEquals(expect, getGroups(text)[group]);
    }
}