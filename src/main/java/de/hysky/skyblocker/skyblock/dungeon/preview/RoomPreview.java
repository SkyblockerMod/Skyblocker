package de.hysky.skyblocker.skyblock.dungeon.preview;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class RoomPreview {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
				dispatcher.register(ClientCommands.literal(SkyblockerMod.NAMESPACE).then(ClientCommands.literal("dungeons")
						.then(ClientCommands.literal("preview").then(ClientCommands.literal("loadRoom").then(argument("type", StringArgumentType.string()).suggests(DungeonManager::suggestRoomTypes)
								.then(argument("room", StringArgumentType.string()).suggests((ctx, sB) -> DungeonManager.suggestRooms(ctx.getArgument("type", String.class), sB))
										.executes(RoomPreview::startPreview))))))));
	}

	private static int startPreview(CommandContext<FabricClientCommandSource> ctx) {
		if (DungeonManager.getRoomBlockData(ctx.getArgument("type", String.class), ctx.getArgument("room", String.class)).isEmpty()) {
			ctx.getSource().sendFeedback(Component.translatable("skyblocker.dungeons.roomPreview.invalidRoom").withStyle(ChatFormatting.RED));
			return -1;
		}

		CLIENT.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
		if (CLIENT.hasSingleplayerServer() && CLIENT.getSingleplayerServer() != null) CLIENT.getSingleplayerServer().stopServer();
		RoomPreviewServer.createServer();
		RoomPreviewServer.loadRoom(ctx.getArgument("type", String.class), ctx.getArgument("room", String.class));
		return Command.SINGLE_SUCCESS;
	}

	static void onJoin() {
		PreviewRoom previewRoom = new PreviewRoom(RoomPreviewServer.selectedRoom);
		DungeonManager.startFromRoomPreview(previewRoom);
	}
}
