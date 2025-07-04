package de.hysky.skyblocker.skyblock.chat.filters;

import org.junit.jupiter.api.Test;

class ImplosionFilterTest extends ChatFilterTest<ImplosionFilter> {
    ImplosionFilterTest() {
        super(new ImplosionFilter());
    }

    @Test
    void oneEnemy() {
        assertMatches("Your Implosion hit 1 enemy for 636,116.8 damage.");
    }

    @Test
    void multipleEnemies() {
        assertMatches("Your Implosion hit 7 enemies for 4,452,817.4 damage.");
    }
}
