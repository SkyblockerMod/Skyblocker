package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcceptRepartyTest extends ChatPatternListenerTest<Reparty> {

    public AcceptRepartyTest() { super(new Reparty()); }

    protected void assertGroup(String message, String group, String expect) {
        Matcher matcher = matcher(message);
        assertTrue(matcher.matches());
        assertEquals(expect, matcher.group(group));
    }

    @Test
    void testDisband() {
        assertGroup("[VIP+] KoloiYolo has disbanded the party!",
                /* group: */ "disband",
                /* expect: */ "KoloiYolo");
    }

    @Test
    void testInvite() {
        assertGroup("-----------------------------------------------------" +
                        "\n[MVP+] 1wolvesgaming has invited you to join their party!" +
                        "\nYou have 60 seconds to accept. Click here to join!" +
                        "\n-----------------------------------------------------",
                /* group: */ "invite",
                /* expect: */ "1wolvesgaming");
    }
}
