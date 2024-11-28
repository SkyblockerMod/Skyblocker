package de.hysky.skyblocker.skyblock.chat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatRulesHandler {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRule.class);
    private static final Path CHAT_RULE_FILE = SkyblockerMod.CONFIG_DIR.resolve("chat_rules.json");
    private static final Codec<Map<String, List<ChatRule>>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, ChatRule.LIST_CODEC);
    /**
     * list of possible locations still formatted for the tool tip
     */
    protected static final List<String> locationsList = List.of (
            "The Farming Islands",
            "Crystal Hollows",
            "Jerry's Workshop",
            "The Park",
            "Dark Auction",
            "Dungeons",
            "The End",
            "Crimson Isle",
            "Hub",
            "Kuudra's Hollow",
            "Private Island",
            "Dwarven Mines",
            "The Garden",
            "Gold Mine",
            "Blazing Fortress",
            "Deep Caverns",
            "Spider's Den",
            "Mineshaft"
    );

    protected static final List<ChatRule> chatRuleList = new ArrayList<>();

    @Init
    public static void init() {
        CompletableFuture.runAsync(ChatRulesHandler::loadChatRules);
        ClientReceiveMessageEvents.ALLOW_GAME.register(ChatRulesHandler::checkMessage);
    }

    private static void loadChatRules() {
        try (BufferedReader reader = Files.newBufferedReader(CHAT_RULE_FILE)) {
            Map<String, List<ChatRule>> chatRules = MAP_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
            LOGGER.info("[Skyblocker Chat Rules]: {}", chatRules);

            chatRuleList.addAll(chatRules.get("rules"));

            LOGGER.info("[Skyblocker Chat Rules] Loaded chat rules");
        } catch (NoSuchFileException e) {
            registerDefaultChatRules();
            LOGGER.warn("[Skyblocker Chat Rules] chat rules file not found, using default rules. This is normal when using for the first time.");
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Chat Rules] Failed to load chat rules file", e);
        }
    }

    private static void registerDefaultChatRules() {
        //clean hub chat
        ChatRule cleanHubRule = new ChatRule("Clean Hub Chat", false, true, true, true, "(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)", "hub", true, false, false, "", null);
        //mining Ability
        ChatRule miningAbilityRule = new ChatRule("Mining Ability Alert", false, true, false, true, "is now available!", "Crystal Hollows, Dwarven Mines", false, false, true, "&1Ability", SoundEvents.ENTITY_ARROW_HIT_PLAYER);

        chatRuleList.add(cleanHubRule);
        chatRuleList.add(miningAbilityRule);
    }

    protected static void saveChatRules() {
        JsonObject chatRuleJson = new JsonObject();
        chatRuleJson.add("rules", ChatRule.LIST_CODEC.encodeStart(JsonOps.INSTANCE, chatRuleList).getOrThrow());
        try (BufferedWriter writer = Files.newBufferedWriter(CHAT_RULE_FILE)) {
            SkyblockerMod.GSON.toJson(chatRuleJson, writer);
            LOGGER.info("[Skyblocker Chat Rules] Saved chat rules file");
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Chat Rules] Failed to save chat rules file", e);
        }
    }

    /**
     * Checks each rule in {@link ChatRulesHandler#chatRuleList} to see if they are a match for the message and if so change outputs based on the options set in the {@link ChatRule}.
     * @param message the chat message
     * @param overlay if its overlay
     */
    private static boolean checkMessage(Text message, boolean overlay) {
        if (!Utils.isOnSkyblock()) return true; //do not work not on skyblock
        if (overlay) return true; //ignore messages in overlay
        String plain =  Formatting.strip(message.getString());

        for (ChatRule rule : chatRuleList) {
            if (rule.isMatch(plain)) {
                //get a replacement message
                Text newMessage;
                if (!rule.getReplaceMessage().isBlank()) {
                    newMessage = formatText(rule.getReplaceMessage());
                } else {
                    newMessage = message;
                }

                if (rule.getShowAnnouncement()) {
                    TitleContainer.addTitle(new Title(newMessage.copy()), SkyblockerConfigManager.get().chat.chatRuleConfig.announcementLength) ;
                }

                //show in action bar
                if (rule.getShowActionBar() && CLIENT.player != null) {
                    CLIENT.player.sendMessage(newMessage, true);
                }

                //show replacement message in chat
                //bypass MessageHandler#onGameMessage to avoid activating chat rules again
                if (!rule.getHideMessage() && CLIENT.player != null) {
                    Utils.sendMessageToBypassEvents(newMessage);
                }

                //play sound
                if (rule.getCustomSound() != null && CLIENT.player != null) {
                    CLIENT.player.playSound(rule.getCustomSound(), 100f, 0.1f);
                }

                //do not send original message
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a string with color codes into a formatted Text object
     * @param codedString the string with color codes in
     * @return formatted text
     */
    protected static MutableText formatText(String codedString) {
        if (codedString.contains(String.valueOf(Formatting.FORMATTING_CODE_PREFIX)) || codedString.contains("&")) {
            MutableText newText =  Text.literal("");
            String[] parts = codedString.split("[" + Formatting.FORMATTING_CODE_PREFIX +"&]");
            Style style = Style.EMPTY;

            for (String part : parts) {
                if (part.isEmpty()) continue;
                Formatting formatting =  Formatting.byCode(part.charAt(0));

                if (formatting != null) {
                    style = style.withFormatting(formatting);
                    Text.literal(part.substring(1)).getWithStyle(style).forEach(newText::append);
                } else {
                    newText.append(Text.of(part));
                }
            }
            return newText;
        }
        return Text.literal(codedString);
    }
}
