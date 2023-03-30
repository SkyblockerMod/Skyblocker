package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.chat.ChatPatternListenerTest;

public class ChatFilterTest<T extends ChatPatternListener> extends ChatPatternListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }
}
