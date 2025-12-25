package de.hysky.skyblocker.skyblock.dungeon.roomPreview;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class RoomPreview {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static boolean isActive = false;
	private static @Nullable Room previewRoom = null;

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("dungeons").
						then(ClientCommandManager.literal("previewRoom").then(argument("type", StringArgumentType.string()).suggests(DungeonManager::suggestRoomTypes)
								.then(argument("room", StringArgumentType.string()).suggests((ctx, sB) -> DungeonManager.suggestRooms(ctx.getArgument("type", String.class), sB))
										.executes(RoomPreview::startPreview)))))));
		ClientPlayConnectionEvents.JOIN.register((nH, pS, client) -> reset());
		WorldRenderExtractionCallback.EVENT.register(RoomPreview::renderWorld);
	}

	private static int startPreview(CommandContext<FabricClientCommandSource> ctx) {
		if (DungeonManager.getRoomBlockData(ctx.getArgument("type", String.class), ctx.getArgument("room", String.class)).isEmpty()) {
			ctx.getSource().sendFeedback(Component.literal("Invalid room!").withStyle(ChatFormatting.RED));
			return -1;
		}

		CLIENT.disconnectFromWorld(Component.empty());
		if (CLIENT.hasSingleplayerServer() && CLIENT.getSingleplayerServer() != null) CLIENT.getSingleplayerServer().stopServer();
		RoomPreviewServer.createServer();
		RoomPreviewServer.loadRoom(ctx.getArgument("type", String.class), ctx.getArgument("room", String.class));
		return Command.SINGLE_SUCCESS;
	}

	private static void reset() {
		isActive = false;
		previewRoom = null;
	}

	private static void renderWorld(PrimitiveCollector primitiveCollector) {
		if (!isActive || previewRoom == null) return;
		previewRoom.extractRendering(primitiveCollector);
	}

	static void onJoin() {
		previewRoom = new PreviewRoom(RoomPreviewServer.selectedRoom);
		DungeonManager.setCurrentRoom(previewRoom);
		DungeonManager.setRunEnded();
		isActive = true;
	}
}
