package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.utils.chat.ChatPatternListenerTest;
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
        assertMatches("§8[§a86§8] §7Advertiser§7: advertisement");
    }

    @Test
    void vip() {
        assertMatches("§8[§b280§8] §a[VIP] Advertiser§f: advertisement");
    }

    @Test
    void mvp() {
        assertMatches("§8[§d256§8] §6§l⚡ §b[MVP§c+§b] Advertiser§f: advertisement");
    }

    @Test
    void plusPlus() {
        assertMatches("§8[§6222§8] §6[MVP§c++§6] Advertiser§f: advertisement");
    }

    @Test
    void capturesMessage() {
        assertGroup("§8[§c325§8] §b[MVP§c+§b] b2dderr§f: buying prismapump", 2, "buying prismapump");
    }

    @Test
    void simpleAd() {
        assertFilters("§8[§e320§8] §b[MVP§c+§b] b2dderr§f: buying prismapump");
    }

    @Test
    void uppercaseAd() {
        assertFilters("§8[§f70§8] §a[VIP] Tecnoisnoob§f: SELLING REJUVENATE 5 Book on ah!");
    }

    @Test
    void characterSpam() {
        assertFilters("§8[§9144§8] §a[VIP] Benyyy_§f: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<");
    }

    @Test
    void notAd() {
        Matcher matcher = listener.pattern.matcher("§8[§6200§8] §a[VIP] NotMatching§f: This message shouldn't match!");
        assertTrue(matcher.matches());
        assertFalse(listener.onMatch(null, matcher));
    }

    void assertFilters(String message) {
        Matcher matcher = listener.pattern.matcher(message);
        assertTrue(matcher.matches());
        assertTrue(listener.onMatch(null, matcher));
    }
}