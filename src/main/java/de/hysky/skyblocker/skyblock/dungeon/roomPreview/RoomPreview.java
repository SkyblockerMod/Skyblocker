package de.hysky.skyblocker.skyblock.dungeon.roomPreview;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static de.hysky.skyblocker.skyblock.dungeon.secrets.Room.SECRET_INDEX;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class RoomPreview {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final List<SecretWaypoint> waypoints = new ArrayList<>();

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
		waypoints.clear();
	}

	private static void renderWorld(PrimitiveCollector primitiveCollector) {
		for (SecretWaypoint waypoint : waypoints) waypoint.extractRendering(primitiveCollector);
	}

	static void onJoin() {
		var roomWaypoints = DungeonManager.getRoomWaypoints(RoomPreviewServer.selectedRoom);
		if (roomWaypoints != null) {
			waypoints.addAll(roomWaypoints.stream().map((waypoint) -> {
				String secretName = waypoint.secretName();
				Matcher secretIndexMatcher = SECRET_INDEX.matcher(secretName);
				int secretIndex = secretIndexMatcher.find() ? Integer.parseInt(secretIndexMatcher.group(1)) : 0;
				BlockPos pos = DungeonMapUtils.relativeToActual(Room.Direction.NW, new Vector2i(0, 0), waypoint);
				return new SecretWaypoint(secretIndex, waypoint.category(), secretName, pos);
			}).toList());
		}

		var customWaypoints = DungeonManager.getCustomWaypoints(RoomPreviewServer.selectedRoom);
		waypoints.addAll(customWaypoints.values());
	}
}
