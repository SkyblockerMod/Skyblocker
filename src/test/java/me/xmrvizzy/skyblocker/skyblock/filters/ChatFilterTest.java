package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListenerTest;

public class ChatFilterTest<T extends ChatPatternListener> extends ChatPatternListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }
}
