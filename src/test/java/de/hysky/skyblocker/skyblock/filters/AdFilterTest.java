package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdFilterTest extends ChatPatternListenerTest<AdFilter> {
    public AdFilterTest() {
        super(new AdFilter());
    }

    @Test
    void noRank() {
        assertMatches("[86] Advertiser: advertisement");
    }

    @Test
    void vip() {
        assertMatches("[280] [VIP] Advertiser: advertisement");
    }

    @Test
    void mvp() {
        assertMatches("[256] ⚡ [MVP+] Advertiser: advertisement");
    }

    @Test
    void plusPlus() {
        assertMatches("[222] [MVP++] Advertiser: advertisement");
    }

    @Test
    void capturesMessage() {
        assertGroup("[325] [MVP+] b2dderr: buying prismapump", 2, "buying prismapump");
    }

    @Test
    void simpleAd() {
        assertFilters("[320] [MVP+] b2dderr: buying prismapump");
    }

    @Test
    void uppercaseAd() {
        assertFilters("[70] [VIP] Tecnoisnoob: SELLING REJUVENATE 5 Book on ah!");
    }

    @Test
    void characterSpam() {
        assertFilters("[144] [VIP] Benyyy_: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<");
    }

    @Test
    void notAd() {
        Matcher matcher = listener.pattern.matcher("[200] [VIP] NotMatching: This message shouldn't match!");
        assertTrue(matcher.matches());
        assertFalse(listener.onMatch(null, matcher));
    }

    void assertFilters(String message) {
        Matcher matcher = listener.pattern.matcher(message);
        assertTrue(matcher.matches());
        assertTrue(listener.onMatch(null, matcher));
    }
}