package de.hysky.skyblocker.skyblock.chat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChatRuleTest {

    @Test
    void isMatch() {
        ChatRule testRule = new ChatRule();
        //test enabled check
        testRule.setFilter("test");
        testRule.setEnabled(false);
        Assertions.assertEquals(testRule.isMatch("test"), false);
        //test simple filter works
        testRule.setEnabled(true);
        Assertions.assertEquals(testRule.isMatch("test"), true);
        //test partial match works
        Assertions.assertEquals(testRule.isMatch("test extra"), false);
        testRule.setPartialMatch(true);
        Assertions.assertEquals(testRule.isMatch("test extra"), true);
        //test ignore case works
        Assertions.assertEquals(testRule.isMatch("TEST"), true);
        testRule.setIgnoreCase(false);
        Assertions.assertEquals(testRule.isMatch("TEST"), false);

        //test regex
        testRule = new ChatRule();
        testRule.setRegex(true);
        testRule.setFilter("[0-9]+");
        Assertions.assertEquals(testRule.isMatch("1234567"), true);
        Assertions.assertEquals(testRule.isMatch("1234567 test"), false);

    }
}