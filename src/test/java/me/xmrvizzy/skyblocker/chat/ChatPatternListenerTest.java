package me.xmrvizzy.skyblocker.chat;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ChatPatternListenerTest<T extends ChatPatternListener> {
    protected final T listener;

    public ChatPatternListenerTest(T listener) {
        this.listener = listener;
    }

    protected Matcher matcher(String message) {
        return listener.pattern.matcher(message);
    }

    protected void assertMatches(String message) {
        assertTrue(matcher(message).matches());
    }

    protected void assertGroup(String message, int group, String expect) {
        Matcher matcher = matcher(message);
        assertTrue(matcher.matches());
        assertEquals(expect, matcher.group(group));
    }
}