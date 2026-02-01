package de.hysky.skyblocker.skyblock.chat;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.JsonData;
import de.hysky.skyblocker.utils.render.gui.BasicToast;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChatRulesHandler {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Path CHAT_RULE_FILE = SkyblockerMod.CONFIG_DIR.resolve("chat_rules.json");

	@VisibleForTesting
	static final Codec<List<ChatRule>> UNBOXING_CODEC = ConfigDataFixer.createDataFixingCodec(ConfigDataFixer.CHAT_RULES_TYPE, CodecUtils.mutableOptional(ChatRule.LIST_CODEC.fieldOf("rules"), ArrayList::new).codec());

	protected static final JsonData<List<ChatRule>> CHAT_RULE_LIST = new JsonData<>(CHAT_RULE_FILE, UNBOXING_CODEC, getDefaultChatRules());

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> CHAT_RULE_LIST.init());
		ClientReceiveMessageEvents.ALLOW_GAME.register(ChatRulesHandler::checkMessage);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
				dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("chatRules")
								.executes(
										Scheduler.queueOpenScreenCommand(() -> new ChatRulesConfigScreen(null)))
		)));
	}

	@VisibleForTesting
	static List<ChatRule> getDefaultChatRules() {
		return new ArrayList<>(List.of(
				new ChatRule("Clean Hub Chat", false, true, true, true, "(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)", EnumSet.of(Location.HUB), true, null, null, null, null, null),
				new ChatRule("Mining Ability Alert", false, true, false, true, "is now available!", EnumSet.of(Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS), false, "&1Ability", null, new ChatRule.AnnouncementMessage("&1Ability", 3000), null, SoundEvents.ARROW_HIT_PLAYER)
		));
	}

	/**
	 * Checks each rule in {@link ChatRulesHandler#CHAT_RULE_LIST} to see if they are a match for the message and if so change outputs based on the options set in the {@link ChatRule}.
	 */
	private static boolean checkMessage(Component message, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		List<ChatRule> rules = CHAT_RULE_LIST.getData();
		if (!CHAT_RULE_LIST.isLoaded() || rules.isEmpty()) return true;
		String plain = ChatFormatting.stripFormatting(message.getString());

		for (ChatRule rule : rules) {
			ChatRule.Match match = rule.isMatch(plain);
			if (!match.matches()) continue;

			// Get a replacement message
			boolean sendOriginal = !rule.getHideMessage();
			if (sendOriginal && rule.getChatMessage() != null) {
				sendOriginal = false;
				Utils.sendMessageToBypassEvents(formatText(match.insertCaptureGroups(rule.getChatMessage())));
			}

			if (rule.getAnnouncementMessage() != null) {
				ChatRule.AnnouncementMessage announcementMessage = rule.getAnnouncementMessage();
				TitleContainer.addTitle(new Title(formatText(match.insertCaptureGroups(announcementMessage.message))), (int) (announcementMessage.displayDuration / 50)); // One tick is 50ms
			}

			// Show in action bar
			if (rule.getActionBarMessage() != null && CLIENT.player != null) {
				CLIENT.player.displayClientMessage(formatText(match.insertCaptureGroups(rule.getActionBarMessage())), true);
			}

			if (rule.getToastMessage() != null) {
				ChatRule.ToastMessage toastMessage = rule.getToastMessage();
				CLIENT.getToastManager().addToast(new BasicToast(formatText(match.insertCaptureGroups(toastMessage.message)), toastMessage.displayDuration, toastMessage.icon));
			}

			// Play sound
			if (rule.getCustomSound() != null && CLIENT.player != null) {
				CLIENT.player.playSound(rule.getCustomSound(), 100f, 0.1f);
			}

			// Do not send the original message
			if (!sendOriginal) return false;
		}
		return true;
	}

	/**
	 * Converts a string with color codes into a formatted Text object
	 *
	 * @param codedString the string with color codes in
	 * @return formatted text
	 */
	protected static MutableComponent formatText(String codedString) {
		// These are done in order of precedence, so § is checked first, then &.
		// This is to ensure that there are no accidental formatting issues due to an actual use of '&' with a valid color code.
		if (codedString.contains("§")) return TextTransformer.fromLegacy(codedString, '§', false);
		if (codedString.contains("&")) return TextTransformer.fromLegacy(codedString, '&', false);
		return Component.literal(codedString);
	}

	public static void saveChatRules() {
		if (CHAT_RULE_LIST.isLoaded()) CHAT_RULE_LIST.save();
	}
}
