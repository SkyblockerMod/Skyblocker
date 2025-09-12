package de.hysky.skyblocker.skyblock.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.JsonData;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class ChatRulesHandler {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Path CHAT_RULE_FILE = SkyblockerMod.CONFIG_DIR.resolve("chat_rules.json");
	private static final Codec<Map<String, List<ChatRule>>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, ChatRule.LIST_CODEC);
	/**
	 * This codec can parse two JSON formats: a list of chat rules OR an object with a "rules" property containing a list of chat rules,
	 * and encodes in one JSON format: an object with a "rules" property containing a list of chat rules.
	 */
	@VisibleForTesting
	static final Codec<List<ChatRule>> UNBOXING_CODEC = Codec.either(ChatRule.LIST_CODEC, MAP_CODEC).xmap(
			either -> either.map(ArrayList::new, map -> new ArrayList<>(map.getOrDefault("rules", getDefaultChatRules()))),
			value -> Either.right(Map.of("rules", value))
	);

	protected static final JsonData<List<ChatRule>> chatRuleList = new JsonData<>(CHAT_RULE_FILE, UNBOXING_CODEC, getDefaultChatRules());

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> chatRuleList.init());
		ClientReceiveMessageEvents.ALLOW_GAME.register(ChatRulesHandler::checkMessage);
	}

	@VisibleForTesting
	static List<ChatRule> getDefaultChatRules() {
		return List.of(
				new ChatRule("Clean Hub Chat", false, true, true, true, "(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)", EnumSet.of(Location.HUB), true, false, false, "", null),
				new ChatRule("Mining Ability Alert", false, true, false, true, "is now available!", EnumSet.of(Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS), false, false, true, "&1Ability", SoundEvents.ENTITY_ARROW_HIT_PLAYER)
		);
	}

	/**
	 * Checks each rule in {@link ChatRulesHandler#chatRuleList} to see if they are a match for the message and if so change outputs based on the options set in the {@link ChatRule}.
	 */
	private static boolean checkMessage(Text message, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		List<ChatRule> rules = chatRuleList.getData();
		if (!chatRuleList.isLoaded() || rules.isEmpty()) return true;
		String plain = Formatting.strip(message.getString());

		for (ChatRule rule : rules) {
			ChatRule.Match match = rule.isMatch(plain);
			if (!match.matches()) continue;

			// Get a replacement message
			Text newMessage;
			if (!rule.getReplaceMessage().isBlank()) {
				newMessage = formatText(match.insertCaptureGroups(rule.getReplaceMessage()));
			} else {
				newMessage = message;
			}

			if (rule.getShowAnnouncement()) {
				TitleContainer.addTitle(new Title(newMessage.copy()), SkyblockerConfigManager.get().chat.chatRuleConfig.announcementLength);
			}

			// Show in action bar
			if (rule.getShowActionBar() && CLIENT.player != null) {
				CLIENT.player.sendMessage(newMessage, true);
			}

			// Show replacement message in chat
			// Bypass MessageHandler#onGameMessage to avoid activating chat rules again
			if (!rule.getHideMessage() && CLIENT.player != null) {
				Utils.sendMessageToBypassEvents(newMessage);
			}

			// Play sound
			if (rule.getCustomSound() != null && CLIENT.player != null) {
				CLIENT.player.playSound(rule.getCustomSound(), 100f, 0.1f);
			}

			// Do not send the original message
			return false;
		}
		return true;
	}

	/**
	 * Converts a string with color codes into a formatted Text object
	 *
	 * @param codedString the string with color codes in
	 * @return formatted text
	 */
	@NotNull
	protected static MutableText formatText(@NotNull String codedString) {
		// These are done in order of precedence, so § is checked first, then &.
		// This is to ensure that there are no accidental formatting issues due to an actual use of '&' with a valid color code.
		if (codedString.contains("§")) return TextTransformer.fromLegacy(codedString, '§', false);
		if (codedString.contains("&")) return TextTransformer.fromLegacy(codedString, '&', false);
		return Text.literal(codedString);
	}

	public static void saveChatRules() {
		if (chatRuleList.getData() != null) chatRuleList.save();
	}
}
