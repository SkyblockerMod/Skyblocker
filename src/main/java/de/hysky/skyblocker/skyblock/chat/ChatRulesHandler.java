package de.hysky.skyblocker.skyblock.chat;

import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.JsonData;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChatRulesHandler {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Path CHAT_RULE_FILE = SkyblockerMod.CONFIG_DIR.resolve("chat_rules.json");

	@VisibleForTesting
	static final Codec<List<ChatRule>> UNBOXING_CODEC = ConfigDataFixer.createDataFixingCodec(ConfigDataFixer.CHAT_RULES_TYPE, CodecUtils.mutableOptional(ChatRule.LIST_CODEC.fieldOf("rules"), ArrayList::new).codec());

	protected static final JsonData<List<ChatRule>> CHAT_RULE_LIST = new JsonData<>(CHAT_RULE_FILE, UNBOXING_CODEC, getDefaultChatRules());

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> CHAT_RULE_LIST.init());
		ClientReceiveMessageEvents.ALLOW_GAME.register(ChatRulesHandler::checkMessage);
	}

	@VisibleForTesting
	static List<ChatRule> getDefaultChatRules() {
		return new ArrayList<>(List.of(
				new ChatRule("Clean Hub Chat", false, true, true, true, "(selling)|(buying)|(lowb)|(visit)|(/p)|(/ah)|(my ah)", EnumSet.of(Location.HUB), true, null, null, null, null, null),
				new ChatRule("Mining Ability Alert", false, true, false, true, "is now available!", EnumSet.of(Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS), false, "&1Ability", null, "&1Ability", null, SoundEvents.ENTITY_ARROW_HIT_PLAYER)
		));
	}

	/**
	 * Checks each rule in {@link ChatRulesHandler#CHAT_RULE_LIST} to see if they are a match for the message and if so change outputs based on the options set in the {@link ChatRule}.
	 */
	private static boolean checkMessage(Text message, boolean overlay) {
		if (overlay || !Utils.isOnSkyblock()) return true;
		List<ChatRule> rules = CHAT_RULE_LIST.getData();
		if (!CHAT_RULE_LIST.isLoaded() || rules.isEmpty()) return true;
		String plain = Formatting.strip(message.getString());

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
				TitleContainer.addTitle(new Title(formatText(match.insertCaptureGroups(rule.getAnnouncementMessage()))), SkyblockerConfigManager.get().chat.chatRuleConfig.announcementLength);
			}

			// Show in action bar
			if (rule.getActionBarMessage() != null && CLIENT.player != null) {
				CLIENT.player.sendMessage(formatText(match.insertCaptureGroups(rule.getActionBarMessage())), true);
			}

			if (rule.getToastMessage() != null) {
				ChatRule.ToastMessage toastMessage = rule.getToastMessage();
				CLIENT.getToastManager().add(new ChatRulesToast(formatText(match.insertCaptureGroups(toastMessage.message)), toastMessage.displayDuration, toastMessage.icon));
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
	@NotNull
	protected static MutableText formatText(@NotNull String codedString) {
		// These are done in order of precedence, so § is checked first, then &.
		// This is to ensure that there are no accidental formatting issues due to an actual use of '&' with a valid color code.
		if (codedString.contains("§")) return TextTransformer.fromLegacy(codedString, '§', false);
		if (codedString.contains("&")) return TextTransformer.fromLegacy(codedString, '&', false);
		return Text.literal(codedString);
	}

	public static void saveChatRules() {
		if (CHAT_RULE_LIST.getData() != null) CHAT_RULE_LIST.save();
	}

	private static class ChatRulesToast implements Toast {
		private static final Identifier TEXTURE = SkyblockerMod.id("notification");

		private final long displayDuration;
		private final ItemStack icon;
		private final List<OrderedText> lines;
		private final int width;
		private Visibility visibility = Visibility.SHOW;

		private ChatRulesToast(Text message, long displayDuration, ItemStack icon) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			this.lines = textRenderer.wrapLines(message, 200);
			this.displayDuration = displayDuration;
			this.icon = icon;
			this.width = lines.stream().mapToInt(textRenderer::getWidth).max().orElse(200) + 30;
			for (OrderedText line : lines) {
				System.out.println(textRenderer.getWidth(line));
			}
		}

		@Override
		public Visibility getVisibility() {
			return visibility;
		}

		@Override
		public void update(ToastManager manager, long time) {
			if (time > displayDuration) visibility = Visibility.HIDE;
		}

		@Override
		public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, getWidth(), getHeight());
			context.drawItemWithoutEntity(icon, 4, 4);
			for (int i = 0; i < lines.size(); i++) {
				context.drawText(textRenderer, lines.get(i), 4 + 16 + 4, 8 + i * 12, -1, false);
			}
		}

		@Override
		public int getHeight() {
			return 8 + 4 + Math.max(lines.size(), 1) * 12;
		}

		@Override
		public int getWidth() {
			return width;
		}
	}
 }
