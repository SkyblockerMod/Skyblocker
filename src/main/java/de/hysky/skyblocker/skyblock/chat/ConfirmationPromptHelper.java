package de.hysky.skyblocker.skyblock.chat;

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

public class ConfirmationPromptHelper {
	private static String command;
	private static long commandFoundAt;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.GAME.register(ConfirmationPromptHelper::onMessage);
		ScreenEvents.AFTER_INIT.register((_client, screen, _scaledWidth, _scaledHeight) -> {
			//Don't check for the command being present in case the user opens the chat before the prompt is sent
			if (Utils.isOnSkyblock() && screen instanceof ChatScreen && SkyblockerConfigManager.get().chat.confirmationPromptHelper) {
				ScreenMouseEvents.beforeMouseClick(screen).register((_screen1, _mouseX, _mouseY, _button) -> {
					if (hasCommand()) {
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

	private static void onMessage(Text message, boolean overlay) {
		if (Utils.isOnSkyblock() && !overlay && SkyblockerConfigManager.get().chat.confirmationPromptHelper && message.getString().contains("[YES]")) {
			Optional<String> confirmationCommand = message.visit((style, asString) -> {
				ClickEvent event = style.getClickEvent();

				//Check to see if its a yes and has the proper command
				if (asString.equals("§a§l[YES]") && event != null && event.getAction() == ClickEvent.Action.RUN_COMMAND && event.getValue().startsWith("/chatprompt")) {
					return Optional.of(event.getValue());
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
	}
}
