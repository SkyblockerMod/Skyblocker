package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;

public class ChatFilterTest<T extends ChatPatternListener> extends ChatPatternListenerTest<T> {
    public ChatFilterTest(T listener) {
        super(listener);
    }
}
