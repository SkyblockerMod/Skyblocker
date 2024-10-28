package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;

public class ChatFilterTest<T extends ChatPatternListener> extends ChatPatternListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }
}
