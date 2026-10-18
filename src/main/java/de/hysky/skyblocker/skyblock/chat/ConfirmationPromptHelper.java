package de.hysky.skyblocker.skyblock.chat;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;

public class ConfirmationPromptHelper {
	public static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationPromptHelper.class);
	private static final List<String> CONFIRMATION_PHRASES = List.of(
			"[Aye sure do!]",    // [NPC] Carnival Pirateman
			"[You guessed it!]",    // [NPC] Carnival Fisherman
			"[Sure thing, partner!]",    // [NPC] Carnival Cowboy
			"[YES]",
			"[Yes]");

	private static String command = "";
	private static long commandFoundAt;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(ConfirmationPromptHelper::onMessage);
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			//Don't check for the command being present in case the user opens the chat before the prompt is sent
			if (Utils.isOnSkyblock() && screen instanceof ChatScreen && SkyblockerConfigManager.get().chat.confirmationPromptHelper) {
				ScreenMouseEvents.beforeMouseClick(screen).register((_, click) -> {
					if (hasCommand()) {
						Minecraft client = Minecraft.getInstance();
						if (client.gui.screen() instanceof ChatScreen) {    // Ignore clicks on other interactive elements
							ActiveTextCollector.ClickableStyleFinder clickHandler = new ActiveTextCollector.ClickableStyleFinder(screen.getFont(), (int) click.x(), (int) click.y())
									.includeInsertions(false);
							Style clickedStyle = clickHandler.result();

							if (clickedStyle != null && clickedStyle.getClickEvent() != null) {    // clicking on some prompts invalidates first prompt but not in all cases, so I decided not to nullify command
								return;
							}
						}

						MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true);
						command = "";
						commandFoundAt = 0;
					}
				});
			}
		});
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> {
			command = "";
			commandFoundAt = 0;
		});
	}

	private static boolean hasCommand() {
		return !command.isEmpty() && commandFoundAt + 60_000 > System.currentTimeMillis();
	}

	private static boolean containsConfirmationPhrase(Component message) {
		String messageStr = ChatFormatting.stripFormatting(message.getString());

		for (String phrase : CONFIRMATION_PHRASES) {
			if (messageStr.contains(phrase)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onMessage(Component message, boolean overlay) {
		if (!Utils.isOnSkyblock() || overlay) return true;
		if (!SkyblockerConfigManager.get().chat.confirmationPromptHelper) return true;

		if (containsConfirmationPhrase(message)) {
			Optional<String> confirmationCommand = message.visit((style, asString) -> {
				ClickEvent event = style.getClickEvent();
				if (event == null) return Optional.empty();

				asString = asString.replaceAll("\\s+", " ").trim();    // clear newline '\n' and trim spaces
				asString = ChatFormatting.stripFormatting(asString);

				// Check to see if it has confirmation phrase and has the proper commands
				if (CONFIRMATION_PHRASES.contains(asString) && event instanceof ClickEvent.RunCommand(String cmd) && (cmd.startsWith("/chatprompt") || cmd.startsWith("/selectnpcoption"))) {
					return Optional.of(cmd);
				}

				return Optional.empty();
			}, Style.EMPTY);

			if (confirmationCommand.isPresent()) {
				command = confirmationCommand.get();
				commandFoundAt = System.currentTimeMillis();

				// Send feedback msg
				Player player = Minecraft.getInstance().player;
				if (player != null)
					player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.chat.confirmationPromptNotification")));
			}
		}

		return true;
	}
}
