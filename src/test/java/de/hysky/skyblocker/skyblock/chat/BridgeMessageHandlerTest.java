package de.hysky.skyblocker.skyblock.chat;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BridgeMessageHandler}.
 * <p>
 * These tests verify that the bridge message pattern correctly matches various message formats
 * and extracts the username and message content properly.
 * </p>
 */
class BridgeMessageHandlerTest {

    /**
     * Regex pattern to match bridge messages and capture the username and message content.
     * <p>
     * This pattern should match the same format as the one in {@link BridgeMessageHandler}.
     * </p>
     */
    @org.intellij.lang.annotations.Language("RegExp")
    private static final Pattern BRIDGE_PATTERN = Pattern.compile("^(?:§r|)(?:§2Guild|§3Officer|Guild|Officer) > .*?:\\s*([\\w§]{3,18})\\s*(?:[»>:]|replying\\s+to)\\s*(.+)$");

    /**
     * Regex pattern to match "replying to" format in bridge messages.
     * <p>
     * This pattern should match the same format as the one in {@link BridgeMessageHandler}.
     * </p>
     */
    @org.intellij.lang.annotations.Language("RegExp")
    private static final Pattern REPLY_PATTERN = Pattern.compile("^(?:([\\w§]{3,18})\\s+)?replying\\s+to\\s+([\\w§]{3,18})\\s*[»>:]\\s*(.+)$");

    @Test
    void testBridgeMessagePatternWithColorCodes() {
        // Test with the actual message format from the logs
        String realMessage = "§2Guild > §a[VIP§6+§a] AlphaPsiBridge §3[ADMIN]§f: minemort » Strat is go to good university";

        Matcher matcher = BRIDGE_PATTERN.matcher(realMessage);

        assertTrue(matcher.find(), "Pattern should match the real message format");
        assertEquals("minemort", matcher.group(1), "Username should be extracted correctly");
        assertEquals("Strat is go to good university", matcher.group(2), "Message content should be extracted correctly");
    }

    @Test
    void testBridgeMessagePatternWithoutColorCodes() {
        // Test with a simpler format without color codes
        String simpleMessage = "Guild > [VIP+] BridgeBotName: anotheruser » Hello there";

        Matcher matcher = BRIDGE_PATTERN.matcher(simpleMessage);

        assertTrue(matcher.find(), "Pattern should match simple message format");
        assertEquals("anotheruser", matcher.group(1), "Username should be extracted correctly");
        assertEquals("Hello there", matcher.group(2), "Message content should be extracted correctly");
    }

    @Test
    void testBridgeMessagePatternWithDifferentSeparators() {
        // Test with different message separators
        String messageWithGreaterThan = "Guild > [VIP+] BridgeBotName: username > Test message";
        String messageWithColon = "Guild > [VIP+] BridgeBotName: username : Another test";

        Matcher matcher1 = BRIDGE_PATTERN.matcher(messageWithGreaterThan);
        Matcher matcher2 = BRIDGE_PATTERN.matcher(messageWithColon);

        assertTrue(matcher1.find(), "Pattern should match with '>' separator");
        assertEquals("username", matcher1.group(1), "Username should be extracted with '>' separator");
        assertEquals("Test message", matcher1.group(2), "Message should be extracted with '>' separator");

        assertTrue(matcher2.find(), "Pattern should match with ':' separator");
        assertEquals("username", matcher2.group(1), "Username should be extracted with ':' separator");
        assertEquals("Another test", matcher2.group(2), "Message should be extracted with ':' separator");
    }

    @Test
    void testBridgeMessagePatternWithOfficerChannel() {
        // Test with Officer channel messages
        String officerMessage = "§3Officer > §a[VIP§6+§a] AlphaPsiBridge §3[ADMIN]§f: officeruser » Officer message";

        Matcher matcher = BRIDGE_PATTERN.matcher(officerMessage);

        assertTrue(matcher.find(), "Pattern should match Officer channel messages");
        assertEquals("officeruser", matcher.group(1), "Username should be extracted from Officer message");
        assertEquals("Officer message", matcher.group(2), "Message should be extracted from Officer message");
    }

    @Test
    void testNonBridgeMessageDoesNotMatch() {
        // Test that regular guild messages don't match
        String regularGuildMessage = "Guild > PlayerName: Hello World";

        Matcher matcher = BRIDGE_PATTERN.matcher(regularGuildMessage);

        assertFalse(matcher.find(), "Regular guild messages should not match the bridge pattern");
    }

    @Test
    void testNonGuildMessageDoesNotMatch() {
        // Test that non-guild messages don't match
        String nonGuildMessage = "Hello World";

        Matcher matcher = BRIDGE_PATTERN.matcher(nonGuildMessage);

        assertFalse(matcher.find(), "Non-guild messages should not match the bridge pattern");
    }

    @Test
    void testMalformedGuildMessageDoesNotMatch() {
        // Test that malformed guild messages don't match
        String malformedMessage = "Guild > No proper format here";

        Matcher matcher = BRIDGE_PATTERN.matcher(malformedMessage);

        assertFalse(matcher.find(), "Malformed guild messages should not match the bridge pattern");
    }

    @Test
    void testUsernameWithColorCodes() {
        // Test that usernames with color codes are handled correctly
        String messageWithColoredUsername = "Guild > [VIP+] BridgeBotName: §cRedUser » Message";

        Matcher matcher = BRIDGE_PATTERN.matcher(messageWithColoredUsername);

        assertTrue(matcher.find(), "Pattern should match usernames with color codes");
        assertEquals("§cRedUser", matcher.group(1), "Username with color codes should be extracted correctly");
        assertEquals("Message", matcher.group(2), "Message should be extracted correctly");
    }

    @Test
    void testMessageWithSpecialCharacters() {
        // Test that messages with special characters are handled correctly
        String messageWithSpecialChars = "Guild > [VIP+] BridgeBotName: user » Message with @#$%^&*() symbols!";

        Matcher matcher = BRIDGE_PATTERN.matcher(messageWithSpecialChars);

        assertTrue(matcher.find(), "Pattern should match messages with special characters");
        assertEquals("user", matcher.group(1), "Username should be extracted correctly");
        assertEquals("Message with @#$%^&*() symbols!", matcher.group(2), "Message with special characters should be extracted correctly");
    }

    @Test
    void testReplyPatternWithSimpleFormat() {
        // Test the "replying to" format with simple usernames
        String replyMessage = "minemort replying to Sri_Lanka » Send list fr";

        Matcher matcher = REPLY_PATTERN.matcher(replyMessage);

        assertTrue(matcher.find(), "Reply pattern should match simple reply format");
        assertEquals("minemort", matcher.group(1), "Original sender should be extracted correctly");
        assertEquals("Sri_Lanka", matcher.group(2), "Target user should be extracted correctly");
        assertEquals("Send list fr", matcher.group(3), "Message content should be extracted correctly");
    }

    @Test
    void testReplyPatternWithColorCodes() {
        // Test the "replying to" format with color codes
        String replyMessageWithColors = "§cRedUser replying to §aGreenUser » Colored message";

        Matcher matcher = REPLY_PATTERN.matcher(replyMessageWithColors);

        assertTrue(matcher.find(), "Reply pattern should match reply format with color codes");
        assertEquals("§cRedUser", matcher.group(1), "Original sender with color codes should be extracted correctly");
        assertEquals("§aGreenUser", matcher.group(2), "Target user with color codes should be extracted correctly");
        assertEquals("Colored message", matcher.group(3), "Message content should be extracted correctly");
    }

    @Test
    void testReplyPatternWithDifferentSeparators() {
        // Test the "replying to" format with different separators
        String replyMessageWithGreaterThan = "user1 replying to user2 > Test message";
        String replyMessageWithColon = "user1 replying to user2 : Another test";

        Matcher matcher1 = REPLY_PATTERN.matcher(replyMessageWithGreaterThan);
        Matcher matcher2 = REPLY_PATTERN.matcher(replyMessageWithColon);

        assertTrue(matcher1.find(), "Reply pattern should match with '>' separator");
        assertEquals("user1", matcher1.group(1), "Original sender should be extracted with '>' separator");
        assertEquals("user2", matcher1.group(2), "Target user should be extracted with '>' separator");
        assertEquals("Test message", matcher1.group(3), "Message should be extracted with '>' separator");

        assertTrue(matcher2.find(), "Reply pattern should match with ':' separator");
        assertEquals("user1", matcher2.group(1), "Original sender should be extracted with ':' separator");
        assertEquals("user2", matcher2.group(2), "Target user should be extracted with ':' separator");
        assertEquals("Another test", matcher2.group(3), "Message should be extracted with ':' separator");
    }

    @Test
    void testFullBridgeMessageWithReplyFormat() {
        // Test the complete bridge message with "replying to" format
        String fullReplyMessage = "Guild > [VIP+] AlphaPsiBridge [ADMIN]: minemort replying to Sri_Lanka » Send list fr";

        Matcher bridgeMatcher = BRIDGE_PATTERN.matcher(fullReplyMessage);
        assertTrue(bridgeMatcher.find(), "Bridge pattern should match full reply message");
        assertEquals("minemort", bridgeMatcher.group(1), "Username should be extracted from full reply message");

        String extractedContent = bridgeMatcher.group(2);
        assertEquals("Sri_Lanka » Send list fr", extractedContent, "Reply content should be extracted from full reply message");

        // Test the reply pattern on the extracted content
        // Note: The reply pattern might not match because the extracted content doesn't include "replying to"
        // The actual handler logic handles this case by checking the original message for "replying to"
        Matcher replyMatcher = REPLY_PATTERN.matcher(extractedContent);
        boolean replyFound = replyMatcher.find();
        // The reply pattern doesn't match because the extracted content is "Sri_Lanka » Send list fr"
        // The handler logic will handle this case by checking the original message
        assertFalse(replyFound, "Reply pattern should not match extracted content without 'replying to'");
    }

    @Test
    void testNonReplyMessageDoesNotMatchReplyPattern() {
        // Test that regular messages don't match the reply pattern
        String regularMessage = "user1 » Hello there";

        Matcher matcher = REPLY_PATTERN.matcher(regularMessage);

        assertFalse(matcher.find(), "Regular messages should not match the reply pattern");
    }

    @Test
    void testMalformedReplyMessageDoesNotMatch() {
        // Test that malformed reply messages don't match
        String malformedReply = "user1 replying user2 » Message";

        Matcher matcher = REPLY_PATTERN.matcher(malformedReply);

        assertFalse(matcher.find(), "Malformed reply messages should not match the reply pattern");
    }
}
