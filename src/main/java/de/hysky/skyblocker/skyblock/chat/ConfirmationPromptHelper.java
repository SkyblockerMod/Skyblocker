package de.hysky.skyblocker.skyblock.chat;

import java.util.List;
import java.util.Optional;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmationPromptHelper {
	public static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationPromptHelper.class);
	private static final List<String> CONFIRMATION_PHRASES = List.of(
			"[Aye sure do!]",	// [NPC] Carnival Pirateman
			"[You guessed it!]",	// [NPC] Carnival Fisherman
			"[Sure thing, partner!]",	// [NPC] Carnival Cowboy
			"YES",
			"Yes");

	// Put here full lines with formatting codes, excluding '\n' and spaces (those are trimmed)
	// It can be extracted by logging asString or from JSON of chat message. Logs also contain it
	private static final List<String> CONFIRMATION_PHRASES_FORMATTING = List.of(
			"§e ➜ §a[Aye sure do!]",	// [NPC] Carnival Pirateman
			"§e ➜ §a[You guessed it!]",	// [NPC] Carnival Fisherman
			"§e ➜ §a[Sure thing, partner!]",	// [NPC] Carnival Cowboy
			"§a§l[YES]",
			"§a[Yes]");

	private static String command;
	private static long commandFoundAt;
	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(ConfirmationPromptHelper::onMessage);
		ScreenEvents.AFTER_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			//Don't check for the command being present in case the user opens the chat before the prompt is sent
			if (Utils.isOnSkyblock() && screen instanceof ChatScreen && SkyblockerConfigManager.get().chat.confirmationPromptHelper) {
				ScreenMouseEvents.beforeMouseClick(screen).register((_screen1, mouseX, mouseY, button) -> {
					if (hasCommand()) {
						MinecraftClient client = MinecraftClient.getInstance();
						if (client.currentScreen instanceof ChatScreen) {	// Ignore clicks on other interactive elements
								Style style = client.inGameHud.getChatHud().getTextStyleAt(mouseX, mouseY);
								if (style != null && style.getClickEvent() != null) {	// clicking on some prompts invalidates first prompt but not in all cases, so I decided not to nullify command
									return;
								}
						}

						MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true);
						command = null;
						commandFoundAt = 0;
					}
				});
			}
		});
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> {
			command = null;
			commandFoundAt = 0;
		});
	}

	private static boolean hasCommand() {
		return command != null && commandFoundAt + 60_000 > System.currentTimeMillis();
	}

	private static boolean containsConfirmationPhrase(Text message) {
		String messageStr = message.getString();
		for (String phrase : CONFIRMATION_PHRASES) {
			if (messageStr.contains(phrase)) {
				return true;
			}
		}
		return false;
	}

	private static boolean onMessage(Text message, boolean overlay) {
		if (Utils.isOnSkyblock() && !overlay && SkyblockerConfigManager.get().chat.confirmationPromptHelper && containsConfirmationPhrase(message)) {
			Optional<String> confirmationCommand = message.visit((style, asString) -> {
				ClickEvent event = style.getClickEvent();
				asString = asString.replaceAll("\\s+", " ").trim();	// clear newline '\n' and trim spaces

				//Check to see if it has confirmation phrase and has the proper commands
				if (CONFIRMATION_PHRASES_FORMATTING.contains(asString) && event instanceof ClickEvent.RunCommand(String command) && (command.startsWith("/chatprompt") || command.startsWith("/selectnpcoption"))) {
					return Optional.of(command);
				}

				return Optional.empty();
			}, Style.EMPTY);

			if (confirmationCommand.isPresent()) {
				command = confirmationCommand.get();
				commandFoundAt = System.currentTimeMillis();

				//Send feedback msg
				MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.chat.confirmationPromptNotification")), false);
			}
		}

		return true;
	}
}
