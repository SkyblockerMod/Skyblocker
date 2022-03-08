package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.chat.ChatPatternListenerTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ChatFilterTest<T extends ChatPatternListener> extends ChatPatternListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }
}
