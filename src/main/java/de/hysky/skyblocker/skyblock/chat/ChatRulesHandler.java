package de.hysky.skyblocker.skyblock.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.shortcut.Shortcuts;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRulesHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRule.class);
    private static final Path CHAT_RULE_FILE = SkyblockerMod.CONFIG_DIR.resolve("chatRules.json");

    protected static final List<ChatRule> chatRuleList = new ArrayList<>();

    public static void init() {
        loadChatRules();
        ClientReceiveMessageEvents.GAME.register(ChatRulesHandler::checkMessage);
    }

    private static void loadChatRules() {
        try (BufferedReader reader = Files.newBufferedReader(CHAT_RULE_FILE)) {
            Type chatRulesType = new TypeToken<Map<String, List<ChatRule>>>() {
            }.getType();
            Map<String, List<ChatRule>> chatRules = SkyblockerMod.GSON.fromJson(reader,chatRulesType);
            chatRuleList.addAll(chatRules.get("rules"));

            LOGGER.info("[Skyblocker] Loaded chat rules");
        } catch (NoSuchFileException e) {
            //todo create default chat rules
            LOGGER.warn("[Skyblocker] chat rule file not found, using default rules. This is normal when using for the first time.");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to load shortcuts file", e);
        }
    }

    protected static void saveChatRules() {
        JsonObject chatRuleJson = new JsonObject();
        chatRuleJson.add("rules", SkyblockerMod.GSON.toJsonTree(chatRuleList));
        try (BufferedWriter writer = Files.newBufferedWriter(CHAT_RULE_FILE)) {
            SkyblockerMod.GSON.toJson(chatRuleJson, writer);
            LOGGER.info("[Skyblocker] Saved chat rules file");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to save chat rules file", e);
        }
    }

    private static void checkMessage(Text message, Boolean overlay) {

    }

}
