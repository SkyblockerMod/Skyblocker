package me.xmrvizzy.skyblocker.skyblock.dwarven;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

class FetchurTest {
    private static final Fetchur fetchur = new Fetchur();

    @Test
    public void patternCaptures() {
        Matcher m = fetchur.getPattern().matcher("§e[NPC] Fetchur§f: its a hint");
        assertTrue(m.matches());
        assertEquals(m.group(1), "a hint");
    }
}