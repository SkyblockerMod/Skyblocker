package de.hysky.skyblocker.skyblock.tabchat;

import com.mojang.brigadier.CommandDispatcher;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TabChat {
		public static final int BUTTON_WIDTH = 45;
		public static final int BUTTON_HEIGHT = 14;
		public static final int BUTTON_GAP = 2;

		static String activeChat = "all";

		@Init
		public static void init() {
				ClientCommandRegistrationCallback.EVENT.register(TabChat::registerCommand);
		}

		private static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext access) {
				dispatcher.register(literal(SkyblockerMod.NAMESPACE)
								.then(literal("tabchatgui")
												.executes(context -> {
														context.getSource().sendFeedback(Component.translatable("skyblocker.tabChat.openingConfig"));
														net.minecraft.client.Minecraft.getInstance().execute(() ->
																		net.minecraft.client.Minecraft.getInstance().setScreen(new TabChatPositionScreen()));
														return 1;
												})));
		}

		public static boolean isEnabled() {
				return SkyblockerConfigManager.get().uiAndVisuals.tabChat.enableTabChat && Utils.isOnSkyblock();
		}

		public static int getButtonX(int screenWidth) {
				int x = SkyblockerConfigManager.get().uiAndVisuals.tabChat.buttonX;
				int totalWidth = 3 * BUTTON_WIDTH + 2 * BUTTON_GAP;
				return Math.max(0, Math.min(x, screenWidth - totalWidth));
		}

		public static int getButtonY(int screenHeight) {
				int y = SkyblockerConfigManager.get().uiAndVisuals.tabChat.buttonY;
				return Math.max(0, Math.min(y, screenHeight - BUTTON_HEIGHT));
		}
}
