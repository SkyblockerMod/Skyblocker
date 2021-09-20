package me.xmrvizzy.skyblocker.chat.filters;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class AdFilterTest {

    private final static Pattern AD_PATTERN = new AdFilter().getPattern();

    @Test
    void noRank() {
        testAd("§7Advertiser§7: buy");
    }

    @Test
    void vip() {
        testAd("§a[VIP] Advertiser§f: buy");
    }

    @Test
    void mvp() {
        testAd("§b[MVP§c+§b] Advertiser§f: buy");
    }

    @Test
    void plusPlus() {
        testAd("§6[MVP§c++§6] Advertiser§f: buy");
    }

    @Test
    void simpleAd() {
        testAd("§b[MVP§c+§b] b2dderr§f: buying prismapump");
    }

    @Test
    void uppercaseAd() {
        testAd("§a[VIP] Tecnoisnoob§f: SELLING REJUVENATE 5 Book on ah!");
    }

    @Test
    void characterSpam() {
        testAd("§a[VIP] Benyyy_§f: Hey, Visit my Island, i spent lots of time to build it! I also made donate room! <<<<<<<<<<<<<<<<<<<");
    }

    @Test
    void notAd() {
        assertFalse(AD_PATTERN.matcher("§a[VIP] NotMatching§f: This message shouldn't match!").matches());
    }

    public void testAd(String ad) {
        assertTrue(AD_PATTERN.matcher(ad).matches());
    }
}