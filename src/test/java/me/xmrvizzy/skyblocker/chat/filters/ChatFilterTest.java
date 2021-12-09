package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.chat.ChatListenerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ChatFilterTest<T extends ChatListener> extends ChatListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }

    protected boolean filters(String text) {
        if(!captures(text))
            return false;
        String[] groups = getGroups(text);
        return listener.onMessage(groups);
    }
    protected void assertFilters(String text) {
        assertTrue(filters(text));
    }
    protected void assertNotFilters(String text) {
        assertFalse(filters(text));
    }
}
