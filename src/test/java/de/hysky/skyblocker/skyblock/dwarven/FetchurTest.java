package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

class FetchurTest extends ChatPatternListenerTest<Fetchur> {
    public FetchurTest() {
        super(new Fetchur());
    }

    @Test
    public void patternCaptures() {
        assertGroup("[NPC] Fetchur: its a hint", 1, "a hint");
    }
}
