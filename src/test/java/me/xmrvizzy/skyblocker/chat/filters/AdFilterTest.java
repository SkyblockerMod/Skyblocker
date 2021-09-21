package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class AdFilterTest extends ChatListenerTest<AdFilter> {
    public AdFilterTest() {
        super(new AdFilter());
    }

    @Test
    void noRank() {
        assertMatches("§7Advertiser§7: buy");
    }

    @Test
    void vip() {
        assertMatches("§a[VIP] Advertiser§f: buy");
    }

    @Test
    void mvp() {
        assertMatches("§b[MVP§c+§b] Advertiser§f: buy");
    }

    @Test
    void plusPlus() {
        assertMatches("§6[MVP§c++§6] Advertiser§f: buy");
    }

    @Test
    void simpleAd() {
        assertMatches("§b[MVP§c+§b] b2dderr§f: buying prismapump");
    }

    @Test
    void uppercaseAd() {
        assertMatches("§a[VIP] Tecnoisnoob§f: SELLING REJUVENATE 5 Book on ah!");
    }

    @Test
    void characterSpam() {
        assertMatches("§a[VIP] Benyyy_§f: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<");
    }

    @Test
    void notAd() {
        assertNotMatches("§a[VIP] NotMatching§f: This message shouldn't match!");
    }
}