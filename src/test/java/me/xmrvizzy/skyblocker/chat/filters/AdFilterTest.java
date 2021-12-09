package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.chat.ChatListenerTest;
import org.junit.jupiter.api.Test;

class AdFilterTest extends ChatFilterTest<AdFilter> {
    public AdFilterTest() {
        super(new AdFilter());
    }

    @Test
    void noRank() {
        assertCaptures("§7Advertiser§7: advertisement");
    }

    @Test
    void vip() {
        assertCaptures("§a[VIP] Advertiser§f: advertisement");
    }

    @Test
    void mvp() {
        assertCaptures("§b[MVP§c+§b] Advertiser§f: advertisement");
    }

    @Test
    void plusPlus() {
        assertCaptures("§6[MVP§c++§6] Advertiser§f: advertisement");
    }

    @Test
    void capturesMessage() {
        assertGroup("§b[MVP§c+§b] b2dderr§f: buying prismapump", 2, "buying prismapump");
    }

    @Test
    void simpleAd() {
        assertFilters("§b[MVP§c+§b] b2dderr§f: buying prismapump");
    }

    @Test
    void uppercaseAd() {
        assertFilters("§a[VIP] Tecnoisnoob§f: SELLING REJUVENATE 5 Book on ah!");
    }

    @Test
    void characterSpam() {
        assertFilters("§a[VIP] Benyyy_§f: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<");
    }

    @Test
    void notAd() {
        assertNotFilters("§a[VIP] NotMatching§f: This message shouldn't match!");
    }
}