package me.xmrvizzy.skyblocker.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ChatListenerTest<T extends ChatListener> {
    private final Pattern pattern;

    public ChatListenerTest(T chatListener) {
        pattern = chatListener.getPattern();
    }

    protected void assertMatches(String text) {
        assertTrue(pattern.matcher(text).matches());
    }

    protected void assertNotMatches(String text) {
        assertFalse(pattern.matcher(text).matches());
    }

    protected String[] getGroups(String text) {
        Matcher matcher = pattern.matcher(text);
        assertTrue(matcher.matches());
        String[] groups = new String[matcher.groupCount() + 1];
        for (int i = 0; i < groups.length; i++)
            groups[i] = matcher.group(i);
        return groups;
    }

    protected void assertGroup(String text, int group, String expect) {
        assertEquals(getGroups(text)[group], expect);
    }
}