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
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class RoomPreview {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static boolean isActive = false;
	private static @Nullable Room previewRoom = null;

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager.literal("dungeons").
						then(ClientCommandManager.literal("previewRoom").then(argument("type", StringArgumentType.string()).then(argument("room", StringArgumentType.string())
								.executes(RoomPreview::startPreview)))))));
		ClientPlayConnectionEvents.JOIN.register((nH, pS, client) -> reset());
		WorldRenderExtractionCallback.EVENT.register(RoomPreview::renderWorld);
	}

	private static int startPreview(CommandContext<FabricClientCommandSource> ctx) {
		CLIENT.disconnect(Text.empty());
		if (CLIENT.isIntegratedServerRunning() && CLIENT.getServer() != null) CLIENT.getServer().stop(true);
		RoomPreviewServer.createServer();
		RoomPreviewServer.setupServer();
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
