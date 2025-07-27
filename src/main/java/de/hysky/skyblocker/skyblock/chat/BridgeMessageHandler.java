package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the reformatting of Discord bridge bot messages in guild chat.
 * <p>
 * This class intercepts messages from Discord bridge bots and reformats them to display as "Bridge > username: message"
 * instead of the original "Guild > [RANK] BotName [TAG]: username » message" format.
 * </p>
 * <p>
 * The bridge support can be enabled/disabled via the configuration, and the bot name can be configured
 * to match specific bridge bots.
 * </p>
 */
public class BridgeMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeMessageHandler.class);

    /**
     * Regex pattern to match bridge messages and capture the username and message content.
     * <p>
     * Examples of supported formats:
     * <ul>
     * <li>"§2Guild > §a[VIP§6+§a] AlphaPsiBridge §3[ADMIN]§f: minemort » Strat is go to good university"</li>
     * <li>"Guild > [VIP+] BridgeBotName: anotheruser » Hello there"</li>
     * </ul>
     * </p>
     * <p>
     * Group 1: The Discord username (3-18 characters, may include color codes)
     * Group 2: The actual message content
     * </p>
     */
    @Language("RegExp")
    private static final Pattern BRIDGE_PATTERN = Pattern.compile("^(?:§r|)(?:§2Guild|§3Officer|Guild|Officer) > .*?:\\s*([\\w§]{3,18})\\s*[»>:]\\s*(.+)$");

    /**
     * Initializes the bridge message handler by registering the message event listener.
     * <p>
     * This method is called automatically by the {@link Init} annotation system.
     * </p>
     */
    @Init
    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(BridgeMessageHandler::onMessage);
    }

    /**
     * Handles incoming chat messages and reformats bridge bot messages.
     * <p>
     * This method is called for every chat message received by the client. It checks if the message
     * matches the bridge pattern and, if so, reformats it to display as "Bridge > username: message"
     * instead of the original format.
     * </p>
     * <p>
     * The method only processes messages when:
     * <ul>
     * <li>The player is on Skyblock</li>
     * <li>The message is not an overlay message</li>
     * <li>Bridge support is enabled in the configuration</li>
     * </ul>
     * </p>
     *
     * @param message the incoming chat message to process
     * @param overlay whether this is an overlay message (system messages, etc.)
     * @return {@code true} to allow the original message to display, {@code false} to suppress it
     */
    private static boolean onMessage(Text message, boolean overlay) {
        if (!Utils.isOnSkyblock() || overlay) {
            return true;
        }

        if (!SkyblockerConfigManager.get().chat.enableBridgeSupport) {
            return true;
        }

        String messageStr = message.getString();
        String configuredBotName = SkyblockerConfigManager.get().chat.bridgeBotName;

        // Debug logging to see what messages we're receiving
        if (messageStr.contains(configuredBotName)) {
            LOGGER.info("[Bridge Debug] Received message containing bot name '{}': '{}'", configuredBotName, messageStr);
        }

        // Also log any message that looks like a bridge message for debugging
        if (messageStr.contains("Guild >") && messageStr.contains("»")) {
            LOGGER.info("[Bridge Debug] Potential bridge message: '{}'", messageStr);
        }

        // Try to match the bridge pattern (handles various formats including color codes)
        Matcher matcher = BRIDGE_PATTERN.matcher(messageStr);
        boolean found = matcher.find();

        if (found) {
            String username = matcher.group(1); // The Discord username
            String messageContent = matcher.group(2); // The actual message

            LOGGER.info("[Bridge Debug] Matched bridge message - Username: '{}', Message: '{}'", username, messageContent);

            // Create the new formatted message
            MutableText newMessage = Text.literal("Bridge > ")
                .formatted(Formatting.DARK_GREEN)
                .append(Text.literal(username + ": " + messageContent).formatted(Formatting.WHITE));

            // Send the reformatted message and suppress the original
            Utils.sendMessageToBypassEvents(newMessage);
            return false; // Suppress the original message
        }

        return true;
    }
}
